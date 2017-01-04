
$resin_url = 'http://www.caucho.com/download/rpm-6.8'
$resin_version = '4.0.49'

def install_dbs(config)
  config.vm.provision :docker, images: %w(mongo mysql) do |d|
    base_path = '/home/jam/Projects/hexagon/systems'
    cnf_volume = "-v #{base_path}/benchmark.cnf:/etc/mysql/conf.d/config-file.cnf"
    sql_volume = "-v #{base_path}/benchmark.sql:/docker-entrypoint-initdb.d/benchmark.sql"
    mysql_args = "-p 3306:3306 -e MYSQL_ALLOW_EMPTY_PASSWORD=yes #{cnf_volume} #{sql_volume}"
    mongo_args = "-p 27017:27017 -v #{base_path}/mongodb.js:/mongodb.js:ro"

    d.run 'mysql', image: 'mysql', args: mysql_args
    d.run 'mongo', image: 'mongo', args: mongo_args
  end
  command config, 'docker exec -it mongo mongo /mongodb.js'
end

def install_resin(config)
  resin = "#{$resin_url}/#{$resin_version}/x86_64/resin-#{$resin_version}-1.x86_64.rpm"
  command config,
    "yum -q -y install #{resin} || true",
    'sudo rm -rf /var/resin/webapps/*',
    'sudo mv -f /home/vagrant/ROOT.war /var/resin/webapps',
    'sudo systemctl start resin',
    'sudo systemctl enable resin',
    'sudo systemctl enable firewalld',
    'sudo systemctl start firewalld',
    'sudo firewall-cmd --permanent --add-port=9090/tcp',
    'sudo firewall-cmd --permanent --add-port=8080/tcp',
    'sudo firewall-cmd --reload'
end

def upload(config, src, dest)
  config.vm.provision :file, source: src, destination: dest
end

def command(config, *commands)
  commands.each do |cmd| config.vm.provision :shell, inline: cmd end
end
