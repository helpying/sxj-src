package third.rewrite.fastdfs.service.impl;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import third.rewrite.fastdfs.GroupState;
import third.rewrite.fastdfs.StorageClient;
import third.rewrite.fastdfs.StorageState;
import third.rewrite.fastdfs.exception.FdfsConnectException;
import third.rewrite.fastdfs.exception.FdfsUnavailableException;
import third.rewrite.fastdfs.proto.handler.ICmdProtoHandler;
import third.rewrite.fastdfs.proto.handler.TrackerDeleteStorageHandler;
import third.rewrite.fastdfs.proto.handler.TrackerGetFetchStorageHandler;
import third.rewrite.fastdfs.proto.handler.TrackerGetStoreStorageHandler;
import third.rewrite.fastdfs.proto.handler.TrackerListGroupsHandler;
import third.rewrite.fastdfs.proto.handler.TrackerListStoragesHandler;
import third.rewrite.fastdfs.service.ITrackerClientService;
import third.rewrite.fastdfs.socket.FdfsSocket;
import third.rewrite.fastdfs.socket.FdfsSocketService;

public class TrackerClientService implements ITrackerClientService {

	private static final String DEFAULT_CHARSET_NAME = "ISO8859-1";

	private String charsetName = DEFAULT_CHARSET_NAME;
	private String[] trackerServerValues;
	private FdfsSocketService fdfsSocketService;

	private Object lock = new Object();
	private Charset charset;
	private final CircularList<TrackerAddressHolder> trackerAddresses = new CircularList<TrackerAddressHolder>();
	private int availableCount;

	public void init() {

		charset = Charset.forName(charsetName);

		String[] parts;

		Set<InetSocketAddress> trackerAddressSet = new HashSet<InetSocketAddress>();
		for (String trackerServersValue : trackerServerValues) {
			if (StringUtils.isBlank(trackerServersValue)) {
				continue;
			}
			parts = StringUtils.split(trackerServersValue, ":", 2);
			if (parts.length != 2) {
				throw new IllegalArgumentException(
						"the value of item \"tracker_server\" is invalid, the correct format is host:port");
			}
			InetSocketAddress address = new InetSocketAddress(parts[0].trim(),
					Integer.parseInt(parts[1].trim()));
			trackerAddressSet.add(address);
		}
		availableCount = trackerAddressSet.size();
		if (availableCount == 0) {
			throw new IllegalArgumentException(
					"item \"tracker_server\"  not found");
		}

		for (InetSocketAddress address : trackerAddressSet) {
			trackerAddresses.add(new TrackerAddressHolder(address));
		}

	}

	/**
	 * 一个ip取不到就取下一个ip的连接，直到所有的ip都取过一遍还没取到报异常
	 * 
	 * @return
	 */
	private FdfsSocket getTrackerSocket() {

		InetSocketAddress trackerAddresse;
		FdfsSocket socket = null;
		TrackerAddressHolder holder;
		for (int i = 0; i < trackerAddresses.size(); i++) {
			holder = trackerAddresses.next();
			// list中不是所有的的都被标记成不可用并且当前被标记成不可用时间小于10分钟的情况下，直接跳到下一个
			if (availableCount != 0
					&& !holder.available
					&& (System.currentTimeMillis() - holder.lastUnavailableTime) < 10 * 60 * 1000) {
				continue;
			}

			trackerAddresse = holder.address;
			try {
				socket = fdfsSocketService.getSocket(trackerAddresse);
				holder.setState(true);
			} catch (FdfsConnectException e) {
				holder.setState(false);
				holder.lastUnavailableTime = System.currentTimeMillis();
			} catch (Exception ignore) {
				
			}
			if (socket != null) {
				return socket;
			}
		}
		
		throw new FdfsUnavailableException("找不到可用的tracker");

	}

	private <T> T process(Socket socket, ICmdProtoHandler<T> handler) {

		try {
			return handler.handle();
		} finally {
			IOUtils.closeQuietly(socket);
		}
	}

	@Override
	public StorageClient getStoreStorage() {
		return getStoreStorage(null);
	}

	@Override
	public StorageClient getStoreStorage(String groupName) {
		FdfsSocket socket = getTrackerSocket();
		ICmdProtoHandler<StorageClient> handler = new TrackerGetStoreStorageHandler(
				socket, groupName, charset);
		return process(socket, handler);
	}

	@Override
	public StorageClient getFetchStorage(String groupName, String path) {
		FdfsSocket socket = getTrackerSocket();
		ICmdProtoHandler<StorageClient> handler = new TrackerGetFetchStorageHandler(
				socket, false, groupName, path, charset);
		return process(socket, handler);

	}

	@Override
	public StorageClient getUpdateStorage(String groupName, String path) {
		FdfsSocket socket = getTrackerSocket();
		ICmdProtoHandler<StorageClient> handler = new TrackerGetFetchStorageHandler(
				socket, true, groupName, path, charset);
		return process(socket, handler);
	}

	@Override
	public GroupState[] listGroups() {
		FdfsSocket socket = getTrackerSocket();
		ICmdProtoHandler<GroupState[]> handler = new TrackerListGroupsHandler(
				socket, charset);
		return process(socket, handler);
	}

	@Override
	public StorageState[] listStorages(String groupName) {
		final String storageIpAddr = null;
		return listStorages(groupName, storageIpAddr);
	}

	@Override
	public StorageState[] listStorages(String groupName, String storageIpAddr) {
		FdfsSocket socket = getTrackerSocket();
		ICmdProtoHandler<StorageState[]> handler = new TrackerListStoragesHandler(
				socket, charset, groupName, storageIpAddr);
		return process(socket, handler);
	}

	@Override
	public void deleteStorage(String groupName, String storageIpAddr) {
		FdfsSocket socket = getTrackerSocket();
		ICmdProtoHandler<Void> handler = new TrackerDeleteStorageHandler(
				socket, charset, groupName, storageIpAddr);
		process(socket, handler);
	}

	/**
	 * @param charsetName
	 *            the charsetName to set
	 */
	public void setCharsetName(String charsetName) {
		this.charsetName = charsetName;
	}

	/**
	 * @param trackerServerValues
	 *            the trackerServerValues to set
	 */
	public void setTrackerServerValues(String[] trackerServerValues) {
		this.trackerServerValues = trackerServerValues;
	}

	/**
	 * @param fdfsSocketService the fdfsSocketService to set
	 */
	public void setFdfsSocketService(FdfsSocketService fdfsSocketService) {
		this.fdfsSocketService = fdfsSocketService;
	}

	private class TrackerAddressHolder {

		private InetSocketAddress address;
		private boolean available;
		private long lastUnavailableTime;

		/**
		 * @param address
		 */
		private TrackerAddressHolder(InetSocketAddress address) {
			super();
			this.address = address;
			this.available = true;
		}

		private void setState(boolean available) {

			synchronized (lock) {
				if (this.available != available) {
					this.available = available;
					if (available) {
						TrackerClientService.this.availableCount++;
					} else {
						TrackerClientService.this.availableCount--;
					}
				}
			}

		}
	}

}
