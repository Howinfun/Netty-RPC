package com.hyf.rpc.netty.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class IpUtil {
	private static final Logger logger = LoggerFactory.getLogger(IpUtil.class);
	/**
	 *获取本机ip地址
	 * @throws Exception
	 */
	public static String getIp() {
		try {
			Enumeration allNetInterfaces = NetworkInterface.getNetworkInterfaces();
			InetAddress ip;
			while (allNetInterfaces.hasMoreElements()) {
				NetworkInterface netInterface = (NetworkInterface) allNetInterfaces
						.nextElement();
				Enumeration addresses = netInterface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					ip = (InetAddress) addresses.nextElement();
					if (ip != null && ip instanceof Inet4Address) {
						if (ip.getHostAddress().startsWith("127") || (ip.getHostAddress().startsWith("192") && ip.getHostAddress().lastIndexOf(".1") != -1)) {
							continue;
						}
						logger.debug("current host ip is" + ip.getHostAddress());
						return ip.getHostAddress();
					}
				}
			}
		} catch (SocketException e) {
			logger.error("get current ip error", e);
		}
		//默认返回本机回送地址
		return "127.0.0.1";
	}
}
