adc Tool
=======
A useful tool for easier android coding

Structure
----------------

cpplibray
    |-------libDataTraJNI    example of data transfer between java and native       
    |-------libyuvWrap       a wrap for libyuv which include java apis   

javalibray
    |----collection
	
			|-------CameraHelpAPI1   wrap for java camera api 1
			|-------CameraHelpAPI2   wrap for java camera api 2
    |----conversion
			|----ImageChange   useful method of image format change 
			|----TextrureMovieEncoder   a encoder for texture and saved as mp4
			|----VaryTools    utils for texture conversion
    |----FileIo
			|----FileHelper	  utils for io
    |----mediaCodec
			|----player   a player use mediacodec  
    |----UI			



How to use
----------------
Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.TongJiangyong:adcTool:Tag'
	}
