/*
 * Copyright (c) 2020 m2049r
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.m2049r.xmrwallet.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

// Nodes stolen from https://moneroworld.com/#nodes

@AllArgsConstructor
public enum DefaultNodes {
    BELDEX6("mainnet.beldex.io:29095"),
    BELDEX5("publicnode5.rpcnode.stream:29095"),
    BELDEX1("publicnode1.rpcnode.stream:29095"),
    BELDEX2 ("publicnode2.rpcnode.stream:29095"),
    BELDEX3 ("publicnode3.rpcnode.stream:29095"),
    BELDEX4("publicnode4.rpcnode.stream:29095"),
    BELDEX("explorer.beldex.io:19091");

//    MONERUJO("nodex.monerujo.io:18081"),
//    XMRTO("node.xmr.to:18081"),
//    SUPPORTXMR("node.supportxmr.com:18081"),
//    HASHVAULT("nodes.hashvault.pro:18081"),
//    MONEROWORLD("node.moneroworld.com:18089"),
//    XMRTW("opennode.xmr-tw.org:18089");
////
    @Getter
    private final String uri;
}
//BELDEX6("35.178.253.9:29095"),