with open('app/src/main/java/com/example/repository/MotorRepository.kt', 'r') as f:
    content = f.read()

target = '''                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("key", IMGBB_API_KEY)
                    .addFormDataPart(
                        "image", 
                        "upload.jpg", 
                        imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                    )
                    .build()
                    
                val requestUrl = "https://api.imgbb.com/1/upload"
                android.util.Log.d("ImgBBUpload", "Actual HTTP request URL: $requestUrl")
                
                val request = Request.Builder()
                    .url(requestUrl)
                    .post(requestBody)
                    .build()'''

replacement = '''                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "image", 
                        "upload.jpg", 
                        imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                    )
                    .build()
                    
                val httpUrl = okhttp3.HttpUrl.parse("https://api.imgbb.com/1/upload")!!.newBuilder()
                    .addQueryParameter("key", IMGBB_API_KEY)
                    .build()
                    
                android.util.Log.d("ImgBBUpload", "Actual HTTP request URL: $httpUrl")
                
                val request = Request.Builder()
                    .url(httpUrl)
                    .post(requestBody)
                    .build()'''

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/repository/MotorRepository.kt', 'w') as f:
    f.write(content)

