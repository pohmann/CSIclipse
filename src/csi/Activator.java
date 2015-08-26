/**
 * Copyright (c) 2015 Peter J. Ohmann and Benjamin R. Liblit
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package csi;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	/**
	 * The plug-in ID
	 */
	public static final String PLUGIN_ID = "CSIPlugin"; //$NON-NLS-1$

	// The shared instance
	private static @Nullable Activator plugin;
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(@Nullable BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(@Nullable BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		assert plugin != null;
		return plugin;
	}
	
	@Override
	protected void initializeImageRegistry(@Nullable ImageRegistry registry){
		super.initializeImageRegistry(registry);
		
		assert registry != null;
		registry.put("icons/clear_global.png", Activator.getImageDescriptor("icons/clear_global.png"));
		registry.put("icons/global.png", Activator.getImageDescriptor("icons/global.png"));
		registry.put("icons/global_small.png", Activator.getImageDescriptor("icons/global_small.png"));
		registry.put("icons/local_small.png", Activator.getImageDescriptor("icons/local_small.png"));
	}
	
	/**
	 * Return an Image object for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the Image object
	 */
	public static Image getImage(String path){
		ImageRegistry registry = getDefault().getImageRegistry();
		Image regImage = registry.get(path);
		
		// either return the image, or, if the image is not yet in the registry, create the descriptor
		if(regImage != null)
			return(regImage);

		registry.put(path, Activator.getImageDescriptor(path));
		return utils.NotNull.check(registry.get(path));
	}

	/**
	 * Return an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return utils.NotNull.check(imageDescriptorFromPlugin(PLUGIN_ID, path));
	}
}
