/*
 * Copyright (c) 2017 m2049r
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

import com.m2049r.xmrwallet.model.PendingTransaction;
import timber.log.Timber;

public class PendingTx {
    public PendingTransaction.Status status;
    final public String error;
    final public long amount;
    final public long dust;
    final public long fee;
    final public String txId;
    final public long txCount;

    public PendingTx(PendingTransaction pendingTransaction) {
        Timber.d("PendingTx before getStatus");
        status = pendingTransaction.getStatus();
        Timber.d("PendingTx before getErrorString");
        error = pendingTransaction.getErrorString();
        Timber.d("PendingTx before getAmount");
        amount = pendingTransaction.getAmount();
        Timber.d("PendingTx before getDust");
        dust = pendingTransaction.getDust();
        Timber.d("PendingTx before getFee");
        fee = pendingTransaction.getFee();
        Timber.d("PendingTx before getFirstTxId");
        txId = pendingTransaction.getFirstTxId();
        Timber.d("PendingTx before getTxCount");
        txCount = pendingTransaction.getTxCount();
        Timber.d("PendingTx after getTxCount");
    }
}
