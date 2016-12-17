
# def upload(config, src, dest)
#   config.vm.provision :file, source: src, destination: dest
# end

def upload(config, *paths)
  paths.each do |src, dest| config.vm.provision :file, source: src, destination: dest end
end

def command(config, *commands)
  commands.each do |cmd| config.vm.provision :shell, inline: cmd end
end
