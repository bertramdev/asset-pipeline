Servlet Asset Pipeline Integration
----------------------------------

This project implements two stand-alone servlet filters:

* AssetPipelineDevFilter, for serving assets processed on demand, with cache-busing headers
* AssetPipelineFilter, for serving pre-compiled assets, handling etags etc.