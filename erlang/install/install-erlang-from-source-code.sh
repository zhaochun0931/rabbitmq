# prerequsite

sudo apt update
sudo apt install build-essential libncurses5-dev libncursesw5-dev libssl-dev pkg-config -y

gcc --version
pkg-config --version







# download and compile the source code

wget https://github.com/erlang/otp/releases/download/OTP-27.3/otp_src_27.3.tar.gz

tar -xzvf otp_src_27.3.tar.gz

cd otp_src_27.3/

./configure --with-ssl

make

make install







