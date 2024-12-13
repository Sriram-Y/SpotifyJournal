import spotipy
from user_cred import CLIENT_ID, CLIENT_SECRET, REDIRECT_URI
from spotipy.oauth2 import SpotifyOAuth
from google.cloud import storage
import json

# Set your Spotify API credentials
client_id = CLIENT_ID
client_secret = CLIENT_SECRET
redirect_uri = REDIRECT_URI

# Authenticate with Spotify
sp = spotipy.Spotify(auth_manager=SpotifyOAuth(client_id=client_id, client_secret=client_secret,
                     redirect_uri=redirect_uri, scope='user-library-read'))

# Function to get all liked songs
def get_liked_songs():
    liked_songs = []
    results = sp.current_user_saved_tracks(limit=50)
    liked_songs.extend(results['items'])

    while results['next']:
        results = sp.next(results)
        liked_songs.extend(results['items'])

    return liked_songs

def upload_to_gcs(bucket_name, data, service_account_file):
    """Uploads the given data to the specified GCS bucket."""
    client = storage.Client.from_service_account_json(service_account_file)
    bucket = client.get_bucket(bucket_name)
    
    blob = bucket.blob('spotify_liked_songs_ids.json')
    
    blob.upload_from_string(json.dumps(data), content_type='application/json')
    print(f"Data successfully uploaded to {bucket_name}/spotify_liked_songs_ids.json")

def main():
    liked_songs = get_liked_songs()

    liked_songs_data = []
    for idx, item in enumerate(liked_songs):
        track = item['track']
        liked_songs_data.append({
            'index': idx,
            'track_id': track['id'],
        })

    service_account_file = 'service_account_key.json'

    upload_to_gcs('spotify-user-data-bucket', liked_songs_data, service_account_file)

if __name__ == "__main__":
    main()
