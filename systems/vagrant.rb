
$resin_url = 'http://www.caucho.com/download/rpm-6.8'
$resin_version = '4.0.48'

def installDbs(config)
  config.vm.provision :docker, images: %w(mongo mysql) do |d|
    base_path = '/home/jam/Projects/hexagon/systems'
    cnf_volume = "-v #{base_path}/benchmark.cnf:/etc/mysql/conf.d/config-file.cnf"
    sql_volume = "-v #{base_path}/benchmark.sql:/docker-entrypoint-initdb.d/benchmark.sql"
    mysql_args = "-p 3306:3306 -e MYSQL_ALLOW_EMPTY_PASSWORD=yes #{cnf_volume} #{sql_volume}"

    d.run 'mysql', image: 'mysql', args: mysql_args
    d.run 'mongo', image: 'mongo', args: '-p 27017:27017'
  end
end

def upload(config, *paths)
  paths.each do |src, dest| config.vm.provision :file, source: src, destination: dest end
end

def command(config, *commands)
  commands.each do |cmd| config.vm.provision :shell, inline: cmd end
end
