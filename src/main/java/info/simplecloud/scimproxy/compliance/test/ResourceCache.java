package info.simplecloud.scimproxy.compliance.test;

import info.simplecloud.core.Resource;

import java.util.ArrayList;
import java.util.List;

public class ResourceCache<T extends Resource> {

	List<T> resources = new ArrayList<T>();

	public ResourceCache() {
		super();
	}

	public void addCachedResource(T cached) {
		this.resources.add(cached);
	}

	public T removeCachedResource() {
		return this.resources.remove(0);
	}

	public int size() {
		return this.resources.size();
	}

	public T borrowCachedResource() {
		return !this.resources.isEmpty() ? this.resources.get(0) : null;
	}
}