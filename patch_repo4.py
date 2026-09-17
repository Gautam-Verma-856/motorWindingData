with open('app/src/main/java/com/example/repository/MotorRepository.kt', 'r') as f:
    content = f.read()

target = '''                val httpUrl = okhttp3.HttpUrl.parse("https://api.imgbb.com/1/upload")!!.newBuilder()
                    .addQueryParameter("key", IMGBB_API_KEY)
                    .build()'''

replacement = '''                val httpUrl = okhttp3.HttpUrl.Builder()
                    .scheme("https")
                    .host("api.imgbb.com")
                    .addPathSegment("1")
                    .addPathSegment("upload")
                    .addQueryParameter("key", IMGBB_API_KEY)
                    .build()'''

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/repository/MotorRepository.kt', 'w') as f:
    f.write(content)

