import spotipy
from user_cred import CLIENT_ID, CLIENT_SECRET, REDIRECT_URI
from spotipy.oauth2 import SpotifyOAuth

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

liked_songs = get_liked_songs()

for idx, item in enumerate(liked_songs):
    track = item['track']
    print(f"{idx + 1}. {track['name']} by {track['artists'][0]['name']}")
