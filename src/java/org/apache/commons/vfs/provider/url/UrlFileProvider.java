/*
 * Copyright 2002, 2003,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs.provider.url;

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemConfigBuilder;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.AbstractFileProvider;
import org.apache.commons.vfs.provider.BasicFileName;
import org.apache.commons.vfs.provider.UriParser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * A file provider backed by Java's URL API.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.21 $ $Date: 2004/05/20 17:40:56 $
 */
public class UrlFileProvider
    extends AbstractFileProvider
{
    protected final static Collection capabilities = Collections.unmodifiableCollection(Arrays.asList(new Capability[]
    {
        Capability.READ_CONTENT,
        Capability.URI,
        Capability.GET_LAST_MODIFIED
    }));

    public UrlFileProvider()
    {
        super();
    }

    /**
     * Locates a file object, by absolute URI.
     */
    public FileObject findFile(final FileObject baseFile,
                               final String uri,
                               final FileSystemOptions fileSystemOptions)
        throws FileSystemException
    {
        try
        {
            StringBuffer buf = new StringBuffer(80);
            String scheme = UriParser.extractScheme(uri, buf);

            final URL url;
            if ("res".equals(scheme))
            {
                String resourceName = buf.toString();

                ClassLoader cl = UrlFileSystemConfigBuilder.getInstance().getClassLoader(fileSystemOptions);
                if (cl == null)
                {
                    cl = getClass().getClassLoader();
                }
                url = cl.getResource(resourceName);

                if (url == null)
                {
                    throw new FileSystemException("vfs.provider.url/badly-formed-uri.error", uri);
                }
            }
            else
            {
                url = new URL(uri);
            }
            final URL rootUrl = new URL(url, "/");
            final String key = this.getClass().getName() + rootUrl.toString();
            FileSystem fs = findFileSystem(key, fileSystemOptions);
            if (fs == null)
            {
                final FileName rootName =
                    // new BasicFileName(scheme, rootUrl.toExternalForm(), FileName.ROOT_PATH);
                    new BasicFileName(rootUrl, FileName.ROOT_PATH);
                fs = new UrlFileSystem(rootName, fileSystemOptions);
                addFileSystem(key, fs);
            }
            return fs.resolveFile(url.getPath());
        }
        catch (final MalformedURLException e)
        {
            throw new FileSystemException("vfs.provider.url/badly-formed-uri.error", uri, e);
        }
    }

    public FileSystemConfigBuilder getConfigBuilder()
    {
        return UrlFileSystemConfigBuilder.getInstance();
    }

    public Collection getCapabilities()
    {
        return capabilities;
    }
}
