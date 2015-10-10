/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package asset.pipeline.ratpack;

import asset.pipeline.AssetPipelineConfigHolder;
import asset.pipeline.ratpack.internal.AssetPropertiesHandler;
import asset.pipeline.ratpack.internal.DevelopmentAssetHandler;
import asset.pipeline.ratpack.internal.ProductionAssetHandler;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.handling.Handlers;

/**
 * @author David Estes
 */
public class AssetPipelineHandler implements Handler {
  public void handle(Context ctx) throws Exception {
    Handler envHandler = AssetPipelineConfigHolder.manifest == null
        ? new DevelopmentAssetHandler()
        : new ProductionAssetHandler();

    ctx.insert(Handlers.chain(
        new AssetPropertiesHandler(),
        envHandler
    ));
  }
}