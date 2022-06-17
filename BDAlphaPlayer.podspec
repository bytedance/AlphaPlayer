Pod::Spec.new do |spec|

  spec.name         = "BDAlphaPlayer"
  spec.version      = "1.2.3"
  spec.summary      = "A player for MP4 with alpha channel."

  spec.homepage     = "https://github.com/bytedance/AlphaPlayer/blob/master/README.md"

  spec.license      = " :type => 'Apache', :file => 'LICENSE"

  spec.author             = { "ByteDance" => "" }

  spec.ios.deployment_target = '9.0'

  spec.source       = { :git => "https://github.com/bytedance/AlphaPlayer.git", :tag => spec.version.to_s }

  spec.source_files  = "iOS/BDAlphaPlayer/*.{h,m,mm}", "iOS/BDAlphaPlayer/**/*.{h,m,mm}"

  spec.resource_bundles = {
     'BDAlphaPlayer' => ['iOS/BDAlphaPlayer/**/*.metal']
   }

  spec.libraries = 'c++'

  spec.frameworks = 'UIKit','CoreVideo'

end
