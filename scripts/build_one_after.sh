#!/bin/sh
#this is only for debugging in Android Studio, when one lib has to rebuild and build all is already done

. ./config.sh
BELDEX_BRANCH=android
BELDEX_SRC_DIR=${WORKDIR}/beldex-android-build
CMAKE_TOOLCHAIN_FILE="${ANDROID_NDK_HOME}/build/cmake/android.toolchain.cmake"
ARCH_ABI=x86_64
cd ${BELDEX_SRC_DIR}/build/release
make wallet_api
make wallet_merged
rm -f /opt/android/Beldex_Wallet/external-libs/${ARCH_ABI}/lib/libwallet_merged.a
mkdir -p /opt/android/Beldex_Wallet/external-libs/${ARCH_ABI}/lib/
cp -vf ${BELDEX_SRC_DIR}/build/release/src/wallet/api/libwallet_merged.a /opt/android/Beldex_Wallet/external-libs/${ARCH_ABI}/lib/
ls -al /opt/android/Beldex_Wallet/external-libs/${ARCH_ABI}/lib/libwallet_merged.a
date

