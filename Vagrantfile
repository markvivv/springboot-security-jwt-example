# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

# Required for the Disks feature
Vagrant.require_version ">= 2.3.7"

# 设置国内镜像
# vagrant box add ubuntu/jammy64 https://mirrors.tuna.tsinghua.edu.cn/ubuntu-cloud-images/jammy/current/jammy-server-cloudimg-amd64-vagrant.box
#BOX_URL = "https://mirrors.tuna.tsinghua.edu.cn/ubuntu-cloud-images/jammy/current/jammy-server-cloudimg-amd64-vagrant.box"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  #config.vm.box_url = BOX_URL
  # only virtualbox
  #config.vm.box = "ubuntu/jammy64"

  # 为 database 定义局部配置，变量为 db
  config.vm.define "redis" do |v|
    v.vm.provider "docker" do |d|
      d.image = "redis"
      d.volumes = ["/tmp/redis:/data"]
      d.ports = ["6379:6379"]
    end
  end
    
  # 为 database 定义局部配置，变量为 db
  config.vm.define "database" do |v|
    v.vm.provider "docker" do |d|
      d.image = "mysql"
      d.volumes = ["/tmp/mysql:/data"]
      d.ports = ["3306:3306"]
      d.env = {
        MYSQL_DATABASE: "example",
        MYSQL_ALLOW_EMPTY_PASSWORD: "yes"
      }
    end
  end

  config.vm.define "web" do |v|
    v.vm.provider "docker" do |d|
      d.build_dir = "."
      #d.remains_running = true
      d.has_ssh = true
    end
  end

end
