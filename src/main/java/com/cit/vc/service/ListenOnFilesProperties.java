package com.cit.vc.service;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cit.vc.utils.Common;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.w3c.dom.events.EventException;

import com.netflix.hystrix.Hystrix;
import com.netflix.hystrix.metric.consumer.HealthCountsStream;

public class ListenOnFilesProperties{

	private static final Logger logger = LoggerFactory.getLogger("filelogger");
	private static final String VERICASH = "vericash";
	private static final String VERICASH_FILE_PROPERTIES = "vericash.properties";

	@Autowired
	private DynamicMappingService dynamicMappingService;

	private Map<String, String> saveAllFileProperties = new ConcurrentHashMap<String, String>();

	public Map<String, String> getSaveAllFileProperties() {
		return saveAllFileProperties;
	}

	public void setSaveAllFileProperties(Map<String, String> saveAllFileProperties) {
		this.saveAllFileProperties = saveAllFileProperties;
	}

	private final WatchService watcher;

	private final Map<WatchKey, Path> keys;

	/**
	 * Creates a WatchService and registers the given directory
	 */
	public ListenOnFilesProperties(Path dir) throws IOException {
		this.watcher = FileSystems.getDefault().newWatchService();
		this.keys = new HashMap<WatchKey, Path>();

		walkAndRegisterDirectories(dir);
	}

	/**
	 * Register the given directory with the WatchService; This function will be
	 * called by FileVisitor
	 */
	private void registerDirectory(Path dir) throws IOException {
		WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		keys.put(key, dir);
	}

	/**
	 * Register the given directory, and all its sub-directories, with the
	 * WatchService.
	 */
	private void walkAndRegisterDirectories(final Path start) throws IOException {
		// register directory and sub-directories
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				registerDirectory(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Process all events for keys queued to the watcher
	 */
	public void processEvents() {
		
		Runnable runnable = new Runnable() {
			
			@Override
			public void run() {
				for (;;) {

					// wait for key to be signalled
					WatchKey key;
					try {
						key = watcher.take();
					} catch (InterruptedException x) {
						return;
					}

					Path dir = keys.get(key);
					if (dir == null) {
						System.err.println("WatchKey not recognized!!");
						continue;
					}

					for (WatchEvent<?> event : key.pollEvents()) {
						@SuppressWarnings("rawtypes")
						WatchEvent.Kind kind = event.kind();

						// Context for directory entry event is the file name of entry
						@SuppressWarnings("unchecked")
						Path name = ((WatchEvent<Path>) event).context();
						Path child = dir.resolve(name);

						if (name.toString().contains("goutputstream")) {
							continue;
						}

						if (kind == ENTRY_CREATE || kind == ENTRY_MODIFY) {
							try {
								if (Files.isDirectory(child)) {
									walkAndRegisterDirectories(child);
								} else {
								// start fix issue "listen to vericash file changes" by IO 05/07/2019
									String dataOfFile = readFileAsString(child.toString());
									saveAllFileProperties.put(name.toString(), dataOfFile);

									dynamicMappingService.setMapVericashProperties(Common.returnProperties(getSaveAllFileProperties().get(VERICASH_FILE_PROPERTIES)));

									String removeExtension = FilenameUtils.removeExtension(name.toString());
									if(removeExtension.equals(VERICASH))
									{
										dynamicMappingService.getMapClientProperties().clear();
										dynamicMappingService.getMapOfVericashAndClient().clear();
									}
									// finish fix issue "listen to vericash file changes" by IO 05/07/2019
									else if (dynamicMappingService.getMapClientProperties().containsKey(removeExtension) ) {
										dynamicMappingService.getMapClientProperties().remove(removeExtension);
										dynamicMappingService.getMapOfVericashAndClient().remove(removeExtension);
									}
									else if (dynamicMappingService.getMapClientResponse().containsKey(removeExtension) ) {
										dynamicMappingService.getMapClientResponse().remove(removeExtension);
										dynamicMappingService.getMapOfVericashAndClientResponse().remove(removeExtension);
									}
									if (dynamicMappingService.getMapOfErrorCodeOfClient().containsKey(removeExtension)) {
										dynamicMappingService.getMapOfErrorCodeOfClient().remove(removeExtension);
									}

									System.out.println(name.toString());
								}
							} catch (IOException x) {
								x.printStackTrace();
							}
						} else if (kind == ENTRY_DELETE) {
							saveAllFileProperties.remove(name.toString());
							String removeExtension = FilenameUtils.removeExtension(name.toString());
							if (dynamicMappingService.getMapClientProperties().containsKey(removeExtension)) {
								dynamicMappingService.getMapClientProperties().remove(removeExtension);
								dynamicMappingService.getMapOfVericashAndClient().remove(removeExtension);
							}
							if (dynamicMappingService.getMapOfErrorCodeOfClient().containsKey(removeExtension)) {
								dynamicMappingService.getMapOfErrorCodeOfClient().remove(removeExtension);
							}
							System.out.println(name.toString() + " : Deleted");
						}
					}

					// reset key and remove from set if directory no longer accessible
					boolean valid = key.reset();
					if (!valid) {
						keys.remove(key);

						// all directories are inaccessible
						if (keys.isEmpty()) {
							break;
						}
					}
				}
			}
		};
		
		Thread thread = new Thread(runnable);
		thread.start();
				
	}

	private String readFileAsString(String fileName) {
		String data = "";
		try {
			data = new String(Files.readAllBytes(Paths.get(fileName)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
	}

}
