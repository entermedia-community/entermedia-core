#!/bin/sh
##Upgrades this open edit installation
export PATH=$PATH:$JAVA_HOME/bin


if [ $1 ]; then
  	PLUGIN=$1
    wget -O install.xml http://dev.openedit.org/anthill/projects/$PLUGIN/install.xml
      echo "Upgrade logs can be read from here: ../upgradelog.txt" 
      echo "Upgrading $PLUGIN...";
      ant -f install.xml > ../upgradelog.txt
      
    case "$1" in
    'openedit-editor')
      	echo "Please merge your /_site.xconf file with the new /_site.xconf.NEW file"
      	;;
    *)
    	;;
  	esac
 
else
        echo "Usage: ./upgrade-plugin.sh NAMEOFPLUGIN"
        echo "Usage: valid plugins are openedit-editor, openedit-cart, openedit-blog, openedit-search, openedit-intranet, etc..."
fi
