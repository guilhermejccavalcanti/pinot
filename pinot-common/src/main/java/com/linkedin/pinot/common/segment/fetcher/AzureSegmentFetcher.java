/**
 * Copyright (C) 2014-2018 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkedin.pinot.common.segment.fetcher;

import com.microsoft.azure.datalake.store.ADLStoreClient;
import com.microsoft.azure.datalake.store.oauth2.AccessTokenProvider;
import com.microsoft.azure.datalake.store.oauth2.ClientCredsTokenProvider;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AzureSegmentFetcher implements SegmentFetcher{
  private static final Logger LOGGER = LoggerFactory.getLogger(AzureSegmentFetcher.class);
  private static final String AZURE = "azure";
  private static final String ACCOUNT = "account";
  private static final String AUTHENDPOINT = "authendpoint";
  private static final String ClIENTID = "clientid";
  private static final String CLIENTSECRET = "clientsecret";

  private ADLStoreClient _adlStoreClient;

  @Override
  public void init(Configuration configs) {
    Configuration azureConfigs = configs.subset(AZURE);
    // The ADL account id. Example: {@code mystore.azuredatalakestore.net}.
    String account = azureConfigs.getString(ACCOUNT);
    // The endpoint that should be used for authentication.
    // Usually of the form {@code https://login.microsoftonline.com/<tenant-id>/oauth2/token}.
    String authEndpoint = azureConfigs.getString(AUTHENDPOINT);
    // The clientId used to authenticate this application
    String clientId = azureConfigs.getString(ClIENTID);
    // The secret key used to authenticate this application
    String clientSecret = azureConfigs.getString(CLIENTSECRET);

    AccessTokenProvider tokenProvider =
        new ClientCredsTokenProvider(authEndpoint, clientId, clientSecret);
    _adlStoreClient = ADLStoreClient.createClient(account, tokenProvider);
  }

  @Override
  public void fetchSegmentToLocal(final String uri, final File tempFile) throws Exception {
    try (InputStream adlStream = _adlStoreClient.getReadStream(uri)) {
      Path dstFilePath = Paths.get(tempFile.toURI());

      /* Copy the source file to the destination directory as a file with the same name as the source,
       * replace an existing file with the same name in the destination directory, if any.
       * Set new file permissions on the copied file.
       */
      Files.copy(adlStream, dstFilePath);

      LOGGER.info("Copied file {} from ADL to {}", uri, tempFile.getAbsolutePath());
    } catch (Exception ex) {
      throw ex;
    }
  }

  @Override
  public Set<String> getProtectedConfigKeys() {
    return Collections.emptySet();
  }
}
