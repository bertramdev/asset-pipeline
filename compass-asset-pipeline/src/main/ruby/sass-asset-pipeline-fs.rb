# EXPIREMENTAL WORK ON FS REPLACEMENT
# NOT CURRENTLY IN USE

require 'sass' #We want to make sure sass is already loaded before we start monkey patching
require 'java'

Sass::Importers::Filesystem.class_eval do
	def find(name, options)
		result = _find(@root, name, options)
		if result
			Java::AssetPipelineSass::SassProcessor.onImport(result.options[:filename])
		end
		return result
	end

	def find_relative(name, base, options)
		result = _find(File.dirname(base), name, options)
		if result
			Java::AssetPipelineSass::SassProcessor.onImport(result.options[:filename])
		end
		return result
	end
end