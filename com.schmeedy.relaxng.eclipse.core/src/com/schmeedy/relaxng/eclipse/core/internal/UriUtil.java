package com.schmeedy.relaxng.eclipse.core.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;

public final class UriUtil {
	private UriUtil() {}
	
	public static String convertRelativePathToResourceUri(String path) {
		if (path.startsWith("/") || path.startsWith("\\")) {
			path = path.substring(1);
		}
		return "platform:/resource/" + path;
	}
	
	/**
	 * 
	 * @param unresolvedUri
	 * @return resolved URI or null if the resolution fails for any reason
	 */
	public static URI resolveUri(String unresolvedUri) {
		URL resolvedURL;
		try {
			resolvedURL = FileLocator.resolve(new URL(unresolvedUri));
			return resolvedURL.toURI();
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		} catch (URISyntaxException e) {}
		
		return null;
	}
	
	public static boolean resourceExists(String unresolvedUri) {
		URI resolvedUri = resolveUri(unresolvedUri);
		if (resolvedUri == null) {
			return false;
		}
		try {
			IFileStore fileStore = EFS.getStore(resolvedUri);
			return fileStore.fetchInfo().exists();
		} catch (CoreException e) {
			return false;
		}
	}
	
	public static InputStream openResource(URI resolvedUri) throws IOException {
		IFileStore fileStore;
		try {
			fileStore = EFS.getStore(resolvedUri);
			return fileStore.openInputStream(EFS.NONE, null);
		} catch (CoreException e) {
			throw new IOException("Exception on attempt to read the schema file. Cause: " + e.getMessage());
		}
	}
	
	public static InputStream openResource(String unresolvedUri) throws IOException {
		URI resolvedUri = resolveUri(unresolvedUri);
		if (resolvedUri == null) {
			throw new FileNotFoundException();
		}
		return openResource(resolvedUri);
	}
	
	public static IFileInfo fetchFileInfo(URI resolvedUri) {
		try {
			return EFS.getStore(resolvedUri).fetchInfo();
		} catch (CoreException e) {
			return new FileInfo();
		}
	}
}
