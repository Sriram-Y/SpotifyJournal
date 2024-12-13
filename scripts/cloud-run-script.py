from google.cloud import storage

def check_bucket_entries(bucket_name, service_account_file):
    client = storage.Client.from_service_account_json(service_account_file)
    
    bucket = client.get_bucket(bucket_name)
    
    blobs = list(bucket.list_blobs())
    
    if len(blobs) > 24:
        print(f"The bucket '{bucket_name}' has more than 24 entries.")
    else:
        print(f"The bucket '{bucket_name}' has {len(blobs)} entries.")

bucket_name = "fitness-data-bucket"
service_account_file = "service_account_key.json"

check_bucket_entries(bucket_name, service_account_file)
