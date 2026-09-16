import re

with open('app/src/main/AndroidManifest.xml', 'r') as f:
    content = f.read()

# Add permission
perm_pattern = r'<uses-permission android:name="android\.permission\.INTERNET" />'
perm_repl = r'<uses-permission android:name="android.permission.INTERNET" />\n    <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />'
content = re.sub(perm_pattern, perm_repl, content)

# Add provider
provider_xml = """        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/provider_paths" />
        </provider>
"""
content = content.replace('    </application>', provider_xml + '    </application>')

with open('app/src/main/AndroidManifest.xml', 'w') as f:
    f.write(content)
print("Patched manifest")
