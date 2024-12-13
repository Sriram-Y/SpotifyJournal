from google.cloud import storage
import re

def get_last_timestamp_entry(contents):
    """Extract and print the highest timestamp and value from file contents."""
    matches = re.findall(r'timestamp_(\d+)=([\d.]+)', contents)
    if not matches:
        print("No timestamps found in the file.")
        return

    timestamp_values = [(int(timestamp), float(value)) for timestamp, value in matches]
    highest_timestamp, value = max(timestamp_values, key=lambda x: x[0])
    print(f"Highest timestamp: {highest_timestamp} with value: {value}")

def check_bucket_entries_fitness(bucket_name, service_account_file):
    """Check entries in a bucket and print data from the last text file if there are more than 24 entries."""
    client = storage.Client.from_service_account_json(service_account_file)
    bucket = client.get_bucket(bucket_name)
    blobs = list(bucket.list_blobs())
    
    if len(blobs) > 24:
        print(f"The bucket '{bucket_name}' has more than 24 entries.")
        
        last_blob = max(blobs, key=lambda blob: blob.updated)
        
        if last_blob.name.endswith('.txt'):
            print(f"Processing the last file in bucket '{bucket_name}': {last_blob.name}")
            contents = last_blob.download_as_text()
            get_last_timestamp_entry(contents)
        else:
            print(f"The last file ('{last_blob.name}') is not a text file.")
    else:
        print(f"The bucket '{bucket_name}' has {len(blobs)} entries.")
        
def check_bucket_entries_spotify(bucket_name, service_account_file):
    """Check if the bucket has more than one entry, if it does, get the latest, and print it out."""
    client = storage.Client.from_service_account_json(service_account_file)
    bucket = client.get_bucket(bucket_name)
    blobs = list(bucket.list_blobs())
    
    if len(blobs) > 1:
        print(f"The bucket '{bucket_name}' has more than one entry.")
        
        last_blob = max(blobs, key=lambda blob: blob.updated)
        
        if last_blob.name.endswith('.txt'):
            print(f"Processing the last file in bucket '{bucket_name}': {last_blob.name}")
            contents = last_blob.download_as_text()
            get_last_timestamp_entry(contents)
        else:
            print(f"The last file ('{last_blob.name}') is not a text file.")
    else:
        print(f"The bucket '{bucket_name}' has {len(blobs)} entries.")
        # TODO: Run a file: /home/srya8501/get-spotify-data.py on the vm called sdc-vm

def main():
    service_account_file = "service_account_key.json"
    
    check_bucket_entries_fitness("fitness-data-bucket", service_account_file)
    check_bucket_entries_spotify("spotify-user-data-bucket", service_account_file)

if __name__ == "__main__":
    main()
