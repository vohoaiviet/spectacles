#!/bin/bash
#This script is to set up the basics for an ec2 instance for it to be ready to run index creation

yum install svn
wget http://cds.sun.com/is-bin/INTERSHOP.enfinity/WFS/CDS-CDS_Developer-Site/en_US/-/USD/VerifyItem-Start/jdk-6u20-linux-i586-rpm.bin?BundledLineItemUUID=zxaJ_hCvYJwAAAEoL2Ymgf5j&OrderID=XdWJ_hCvAxUAAAEoJGYmgf5j&ProductID=guBIBe.oc_wAAAEnaDJHqPYe&FileName=/jdk-6u20-linux-i586-rpm.bin
mv jdk-6u20-linux-i586-rpm.bin?BundledLineItemUUID=zxaJ_hCvYJwAAAEoL2Ymgf5j&OrderID=XdWJ_hCvAxUAAAEoJGYmgf5j&ProductID=guBIBe.oc_wAAAEnaDJHqPYe&FileName=/jdk-6u20-linux-i586-rpm.bin jdk-6u20-linux-i586-rpm.bin
chmod +x jdk-6u20-linux-i586-rpm.bin
./jdk-6u20-linux-i586-rpm.bin
export JAVA_HOME=/usr/java/latest
export PATH=$JAVA_HOME/bin:$PATH
yum install ant
export SVN_EDITOR=/bin/vi
mount -t ext3 /dev/sdc10 /mnt/as/
/etc/init.d/mysql start