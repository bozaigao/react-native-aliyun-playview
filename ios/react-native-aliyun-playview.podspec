require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name         = package['name']
  s.version      = package['version']
  s.summary      = package['description']

  s.authors      = { 'Bozaigao' => '1054539528@qq.com' }
  s.homepage     = package['homepage']
  s.license      = package['license']
  s.platform     = :ios, "8.0"

  s.source       = { :git => "https://github.com/bozaigao/react-native-aliyun-playview" }
  s.source_files  = "ios/AliyunPlayView/**/*.{h,m}"

  s.dependency 'React'
  s.dependency 'VODUpload'
end
