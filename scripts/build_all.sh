#!/bin/sh

. ./config.sh
BELDEX_BRANCH=master
BELDEX_SRC_DIR=${WORKDIR}/beldex-android-build
CMAKE_TOOLCHAIN_FILE="${ANDROID_NDK_HOME}/build/cmake/android.toolchain.cmake"
rm -rf $BELDEX_SRC_DIR
git clone https://github.com/Beldex-Coin/beldex ${BELDEX_SRC_DIR} --branch ${BELDEX_BRANCH} --recursive --depth=1

Set environment variables
ANDROID_NDK_REVISION=20b
ANDROID_NDK_HASH=5dfbbdc2d3ba859fed90d0e978af87c71a91a5be1f6e1c40ba697503d48ccecd
INSTALL_PATH=/opt/android

# Create the installation directory if it doesn't exist
mkdir -p $INSTALL_PATH

# Download and install Android NDK to the specified path
curl -s -O https://dl.google.com/android/repository/android-ndk-r${ANDROID_NDK_REVISION}-linux-x86_64.zip
echo "${ANDROID_NDK_HASH}  android-ndk-r${ANDROID_NDK_REVISION}-linux-x86_64.zip" | sha256sum -c
unzip android-ndk-r${ANDROID_NDK_REVISION}-linux-x86_64.zip -d $INSTALL_PATH
rm -f android-ndk-r${ANDROID_NDK_REVISION}-linux-x86_64.zip

mkdir -p /opt/android/Beldex_Wallet/external-libs/
cp -vf ${BELDEX_SRC_DIR}/src/wallet/api/wallet2_api.h /opt/android/Beldex_Wallet/external-libs/
cd $BELDEX_SRC_DIR
for arch in "aarch" "aarch64" "i686" "x86_64" 
do
	FLAGS=""
	PREFIX=${WORKDIR}/prefix_${arch}
	DEST_LIB_DIR=${PREFIX}/lib
	DEST_INCLUDE_DIR=${PREFIX}/include
	export CMAKE_INCLUDE_PATH="${PREFIX}/include"
	export CMAKE_LIBRARY_PATH="${PREFIX}/lib"

	rm -rf ${PREFIX}
	mkdir -p $DEST_LIB_DIR
	mkdir -p $DEST_INCLUDE_DIR

	case $arch in
	"aarch"	)
		ANDROID_CLANG=armv7a-linux-androideabi${API}-clang
		ANDROID_CLANGPP=armv7a-linux-androideabi${API}-clang++
		BUILD_64=OFF
		BUILD_32=ON
		TAG="android-armv7"
		ARCH="armv7a"
		ARCH_ABI="armeabi-v7a"
		FLAGS="-D CMAKE_ANDROID_ARM_MODE=ON -D NO_AES=true";;
	"aarch64"	)
		ANDROID_CLANG=aarch64-linux-androideabi${API}-clang
		ANDROID_CLANGPP=aarch64-linux-androideabi${API}-clang++
		BUILD_64=ON
		BUILD_32=OFF
		TAG="android-armv8"
		ARCH="armv8-a"
		ARCH_ABI="arm64-v8a";;
	"i686"		)
		ANDROID_CLANG=i686-linux-androideabi${API}-clang
		ANDROID_CLANGPP=i686-linux-androideabi${API}-clang++
		BUILD_64=OFF
		BUILD_32=ON
		TAG="android-x86"
		ARCH="i686"
		ARCH_ABI="x86";;
	"x86_64")
		ANDROID_CLANG=x86_64-linux-androideabi${API}-clang
		ANDROID_CLANGPP=x86_64-linux-androideabi${API}-clang++
		BUILD_64=ON
		BUILD_32=OFF
		TAG="android-x86_64"
		ARCH="x86-64"
		ARCH_ABI="x86_64"
		;;
	esac

	cd $BELDEX_SRC_DIR
	rm -rf ./build/release
	mkdir -p ./build/release
	export PKG_CONFIG_PATH="/usr/include/lib/pkgconfig"
	CC=clang CXX=clang++
	cd ./build/release
	cmake \
    -DCMAKE_CXX_FLAGS=-fdiagnostics-color=always \
    -DCMAKE_C_FLAGS=-fdiagnostics-color=always \
    -DCMAKE_BUILD_TYPE=Release \
    -DCMAKE_TOOLCHAIN_FILE=${CMAKE_TOOLCHAIN_FILE} \
    -DANDROID_PLATFORM=21 \
    -DANDROID_ABI=$ARCH_ABI \
    -DMONERO_SLOW_HASH=ON \
    -DWARNINGS_AS_ERRORS=OFF \
    -DBUILD_TESTS=OFF \
    -DLOCAL_MIRROR=https://builds.lokinet.dev/deps \
    -DBUILD_STATIC_DEPS=ON \
	-DARCH=$ARCH \
    -DARCH_ID=$ARCH \
    ${FLAGS} ../..
	make wallet_api -j4
	# make wallet_merged -j4

	mkdir -p /opt/android/Beldex_Wallet/external-libs/${ARCH_ABI}/lib/
	ln -s ${BELDEX_SRC_DIR}/build/release/src/wallet/api/libwallet_merged.a /opt/android/Beldex_Wallet/external-libs/${ARCH_ABI}/lib/
	mkdir -p /opt/android/Beldex_Wallet/external-libs/${ARCH_ABI}/build-${ARCH_ABI}/
	cp -rvf ${BELDEX_SRC_DIR}/build/release/static-deps /opt/android/Beldex_Wallet/external-libs/${ARCH_ABI}/build-${ARCH_ABI}

done
