from google.cloud import storage
import re
import spotipy
from user_cred import CLIENT_ID, CLIENT_SECRET, REDIRECT_URI
from spotipy.oauth2 import SpotifyOAuth
import json
from datetime import datetime

# Set your Spotify API credentials
sp = spotipy.Spotify(auth_manager=SpotifyOAuth(
    client_id=CLIENT_ID, 
    client_secret=CLIENT_SECRET,
    redirect_uri=REDIRECT_URI, 
    scope='playlist-modify-private user-top-read'))

def get_latest_entry(bucket_name, service_account_file):
    """Fetch and process the latest file from the specified bucket."""
    client = storage.Client.from_service_account_json(service_account_file)
    bucket = client.get_bucket(bucket_name)
    blobs = list(bucket.list_blobs())

    if not blobs:
        print(f"The bucket '{bucket_name}' is empty.")
        return None

    latest_blob = max(blobs, key=lambda blob: blob.updated)

    print(f"Processing the latest file in bucket '{bucket_name}': {latest_blob.name}")
    contents = latest_blob.download_as_text()
    return contents

def process_fitness_data(contents):
    """Extract timestamp-value pairs from the fitness data."""
    matches = re.findall(r'timestamp_(\d+)=([\d.]+)', contents)
    if not matches:
        print("No timestamps found in the file.")
        return []

    return [(int(timestamp), float(value)) for timestamp, value in matches]

def process_spotify_data(contents):
    """Parse the Spotify JSON data."""
    try:
        return json.loads(contents)
    except json.JSONDecodeError as e:
        print(f"Failed to parse JSON data: {e}")
        return []

def get_user_top_tracks():
    """Retrieve the user's top tracks from Spotify."""
    top_tracks = sp.current_user_top_tracks(limit=50, time_range='medium_term')
    return {track['id']: index for index, track in enumerate(top_tracks['items'])}

def create_playlist_with_tracks(track_ids):
    """Create a new Spotify playlist and add the provided tracks."""
    current_date = datetime.now().strftime("%Y-%m-%d")
    playlist_name = f"Spotify Journal {current_date}"

    # Create a new private playlist
    playlist = sp.user_playlist_create(sp.me()['id'], playlist_name, public=False)
    playlist_id = playlist['id']

    # Add tracks to the playlist
    sp.playlist_add_items(playlist_id, track_ids)
    print(f"Playlist '{playlist_name}' created with {len(track_ids)} tracks.")

def main():
    service_account_file = "service_account_key.json"

    fitness_data_contents = get_latest_entry("fitness-data-bucket", service_account_file)
    spotify_data_contents = get_latest_entry("spotify-user-data-bucket", service_account_file)

    if not fitness_data_contents or not spotify_data_contents:
        print("Failed to retrieve data from one or both buckets.")
        return

    fitness_data = process_fitness_data(fitness_data_contents)
    spotify_data = process_spotify_data(spotify_data_contents)

    if not fitness_data or not spotify_data:
        print("No valid data to process.")
        return

    user_top_tracks = get_user_top_tracks()

    used_track_ids = set()
    selected_tracks = []

    for _, value in fitness_data:
        # Filter out tracks that have already been selected
        available_tracks = [track for track in spotify_data if track['track_id'] not in used_track_ids]

        if not available_tracks:
            print("No more unique tracks available in Spotify data.")
            break

        if value < 60:
            track = min(available_tracks, key=lambda x: user_top_tracks.get(x['track_id'], float('inf')))
        else:
            track = max(available_tracks, key=lambda x: user_top_tracks.get(x['track_id'], float('-inf')))

        if track:
            used_track_ids.add(track['track_id'])
            selected_tracks.append(track['track_id'])

    # Create a Spotify playlist with the selected tracks
    create_playlist_with_tracks(selected_tracks)

if __name__ == "__main__":
    main()
