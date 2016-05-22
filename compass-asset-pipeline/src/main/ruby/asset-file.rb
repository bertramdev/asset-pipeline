# EXPIREMENTAL WORK ON FS REPLACEMENT
# NOT CURRENTLY IN USE

require 'pathname'
$SASS_WORK_PATH='./sass-work'
if !defined?(AssetFile)
	class AssetFile < File

		def initialize(filename, mode="r", opt=nil, &block)

			if filename.index("/assets") == 0
				@path = filename
				@is_asset_file = true
				@asset_file = Java::AssetPipeline::AssetHelper.fileForFullName(filename[7..-1])
				if block
					yield self
				end
			else
				super(filename, mode, opt)
			end
		end

		def self.exist?(filename)
			if filename.index("/assets") == 0
				asset_file = Java::AssetPipeline::AssetHelper.fileForFullName(filename[7..-1])
				return true if asset_file
			end
			super(filename)
		end

		def self.exists?(filename)
			if filename.index("/assets") == 0
				asset_file = Java::AssetPipeline::AssetHelper.fileForFullName(filename[7..-1])
				return true if asset_file
			end
			super(filename)
		end

		def self.delete(file_name)
			if filename.index("/assets") == 0
				asset_file = Java::AssetPipeline::AssetHelper.fileForFullName(filename[7..-1])
				return true if asset_file
			end
			super(file_name)
		end

		def self.ctime(file_name)
			if filename.index("/assets") == 0
				asset_file = Java::AssetPipeline::AssetHelper.fileForFullName(filename[7..-1])
				return Date.new if asset_file
			end
			super(file_name)
		end



		def self.read(path,*a)
			offset=0
			length=nil
			if path and path.index("/assets") == 0

				asset_file = Java::AssetPipeline::AssetHelper.fileForFullName(path[7..-1])
				if asset_file
					stream = asset_file.getInputStream()
					buffer = ''
					position = 0
					c = stream.read()
					while(c > -1) do
						if offset == nil || position >= offset
							if length == nil
								buffer += c.chr
							elsif length != nil && position < length+offset
								buffer += c.chr
							end
						end
						position++
						c = stream.read()
					end
					stream.close()
					return buffer
				end
			end
			super(path, *a)
		end

		def self.readable?(pathname)
			if pathname and pathname.index("/assets") == 0
				asset_file = Java::AssetPipeline::AssetHelper.fileForFullName(pathname[7..-1])
				return true if asset_file
			end
			super(pathname)
		end

		def read
			if @asset_file
				stream = @asset_file.getInputStream()
				buffer = ''
				c = stream.read()
				while(c > -1) do
					buffer += c.chr
					c = stream.read()
				end
				stream.close()
				return buffer
			else
				super()
			end
		end

		def realpath
			if @asset_file
				return "/assets/#{@asset_file.getCanonicalPath()}"
			end
			return super()
		end

		def path
			if @asset_file
				return "/assets/#{@asset_file.getCanonicalPath()}"
			end
			return super()
		end

		def close
			if @asset_file
				return
			end
			super()
		end
	end

	class AssetDir < Dir
		def self.[](pattern)
			if pattern.index('/assets/') == 0
				new_pattern = pattern[8..-1]
				new_pattern = Java::AssetPipeline::AssetHelper.normalizePath(new_pattern)
				paths = []
				resolvers = Java::AssetPipeline::AssetPipelineConfigHolder.resolvers
				resolvers.each do |resolver|
					resolver.scanForFiles(['**/*'],[new_pattern]).each do |file|
						paths << "/assets/#{file.getCanonicalPath()}"
					end
				end
				return paths
			end
			super(pattern)
			
		end
	end


	class AssetPathname < Pathname
		def realpath
			if @path and @path.index("/assets") == 0
				return Java::AssetPipeline::AssetHelper.normalizePath(@path)
			end
			super()
		end
	end
	RubyPathname = Pathname
	RubyFile = File
	RubyDir = Dir
	File = AssetFile
	Dir = AssetDir
	Pathname = AssetPathname
end