---
layout: default
title: "Target: iOS (haxe)"
---

## Info

TODO

## Certificates

Generate key:
{% highlight bash %}
openssl genrsa -out myprivkey.key 2048
{% endhighlight %}

Generate CSR:
{% highlight bash %}
openssl req -new -sha256 -key myprivkey.key -out request.csr
{% endhighlight %}

Using CSR, generate a Certificate and then a Provisioning Profile:
* [https://developer.apple.com/account/ios/certificate/](https://developer.apple.com/account/ios/certificate/) (.cer file)
* [https://developer.apple.com/account/ios/profile/](https://developer.apple.com/account/ios/profile/) (.mobileprovision file)

[Generate p12 from cer + key](http://help.adobe.com/en_US/as3/iphone/WS144092a96ffef7cc-371badff126abc17b1f-7fff.html):
{% highlight bash %}
openssl x509 -in ios_development.cer -inform DER -out developer_identity.pem -outform PEM
openssl pkcs12 -export -inkey myprivkey.key -in developer_identity.pem -out iphone_dev.p12
{% endhighlight %}

Files involved:

* `myprivkey.key`
* `request.csr`
* `ios_development.cer`
* `file.mobileprovision`
* `developer_identity.pem`
* `iphone_dev.p12`

Package Adobe AIR APP:

{% highlight bash %}
adt -package -target ipa-app-store -storetype pkcs12 -keystore $P12 -provisioning-profile $MOBILEPROFILE $name.ipa app.xml $name.swf assets
{% endhighlight %}
