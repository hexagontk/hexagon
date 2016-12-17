
$project_name = 'benchmark'
$resin_url = 'http://www.caucho.com/download/rpm-6.8'
$resin_version = '4.0.49'

def installDbs(config)
  config.vm.provision :docker, images: %w(mongo mysql) do |d|
    d.run 'mongo', image: 'mongo', args: '-p 27017:27017'
    d.run 'mysql', image: 'mysql', args: '-p 3306:3306 -e MYSQL_ALLOW_EMPTY_PASSWORD=yes'
  end
end

def upload(config, *paths)
  paths.each do |src, dest| config.vm.provision :file, source: src, destination: dest end
end

def command(config, *commands)
  commands.each do |cmd| config.vm.provision :shell, inline: cmd end
end
