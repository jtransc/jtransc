# this is a comment
-D
custombuildtestwork2
{% if (tempAssetsDir + "/filetoinclude.txt" )|file_exists %}
-D
custombuildtestwork_file1
{% end %}
{% if (tempAssetsDir + "/filetoinclude2.txt" )|file_exists %}
-D
custombuildtestwork_file2
{% end %}
{% if (tempAssetsDir + "/filetoinclude3.txt" )|file_exists %}
-D
custombuildtestwork_file3
{% end %}
----
echo
Other command!
----
echo
Other more command!
----
