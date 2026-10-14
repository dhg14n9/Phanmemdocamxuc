import re

with open("app/src/main/java/com/example/ui/AuraViewModel.kt", "r") as f:
    content = f.read()

pattern = r'''            val cm = getApplication<android\.app\.Application>\(\)\.getSystemService\(android\.content\.Context\.CONNECTIVITY_SERVICE\) as android\.net\.ConnectivityManager
            val isOffline = cm\.activeNetwork == null \|\| !cm\.getNetworkCapabilities\(cm\.activeNetwork\)!!\.hasCapability\(android\.net\.NetworkCapabilities\.NET_CAPABILITY_INTERNET\)'''

replacement = r'''            val cm = getApplication<android.app.Application>().getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            val net = cm.activeNetwork
            val caps = if (net != null) cm.getNetworkCapabilities(net) else null
            val isOffline = caps == null || !caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)'''

content = re.sub(pattern, replacement, content, count=1)

with open("app/src/main/java/com/example/ui/AuraViewModel.kt", "w") as f:
    f.write(content)
