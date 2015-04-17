# kulu-mobile
The mobile app for "kulu" (Estonian for expense)

Change your environment settings in `strings.xml`. They are set to development by default. The backend URLs are set to the VirtualBox Host-Only IP, in assumption that you're probably using GenyMotion to run the app.

    <string name="kulu_s3_tmp_bucket">dev_kulu_invoices</string>
    <string name="kulu_s3_access_key_id">AKIAIQRWHSDWA6CFUGIA</string>
    <string name="kulu_s3_secret_access_key"> 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</string>
    <string name="kulu_backend_service_url">http://192.168.56.1:3001/invoices</string>
    <string name="kulu_backend_login_url">http://192.168.56.1:3001/login</string>
