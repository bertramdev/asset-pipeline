require 'compass'
require 'java'

Compass::Compiler.class_eval do 
    def engine(sass_filename, css_filename)
      syntax = (sass_filename =~ /\.(s[ac]ss)$/) && $1.to_sym || :sass
      opts = sass_options.merge(:filename => sass_filename, :css_filename => css_filename, :syntax => syntax)
      asset_file = Java::AssetPipeline::AssetHelper.fileForFullName(sass_filename)
      Sass::Engine.new(Java::AssetPipelineSass::SassProcessor.convertStreamToString(asset_file.getInputStream()), opts)
    end
end

Compass::SpriteImporter.class_eval do
end