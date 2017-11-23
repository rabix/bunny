package org.rabix.engine.store.event.sourcing;

import org.rabix.engine.store.postgres.jdbi.JDBIRepositoryRegistry;

public abstract class EventSourcingTransactionHelper extends JDBIRepositoryRegistry {

    @Override
    public <Result> Result doInTransaction(TransactionCallback<Result> callback) throws Exception {
        return callback.call();
    }
}
