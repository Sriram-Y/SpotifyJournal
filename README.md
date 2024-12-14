# How to run this code
**Hardware**
- You will need a Google Pixel Watch to run it as intended for a end user. But you can run it in an Android emulator and set it up to emulate a Google Pixel Watch 2
- Clone the repo, open the project in Android studio. If you have a watch, connect your watch in the devices section. If you are just testing, use an emulator.

**Software**
- Once the VMs are created. You will need to load the scripts to them and set them up to launch on startup.
- SSH into the `sdc-vm` using the console and upload `get-spotify-data.py` and `startup-script.sh`. Set `startup-script.sh` to launch on startup. You can use this tutorial [here](https://www.baeldung.com/linux/run-script-on-startup).
- SSH into the `pe-vm` using the console and upload `create-playist.py` and `startup-script.sh`. Set `startup-script.sh` to launch on startup. You can use this tutorial [here](https://www.baeldung.com/linux/run-script-on-startup).
- We will assume the cloud run function is already setup to run the script `cloud-run-function.py`. If not, create one and set it to run every hour.

**Building and running**
- In Android Studio, sync Gradle, build, and run the app on your target.
- If VM's are detected on the GCP Project, you will quickly see a message that says VMs were created succesfully. Otherwise it will take some time but you can continue using the watch as normal. All the work will now occur in the background.
- On the `sdc-vm` if will have to authenticate your spotify account. Visit `http://localhost` on the machine and follow the steps.
- You can now continue to use your device as normal. After 24 hours you should see your journal playlist appear wherever you use Spotify.
