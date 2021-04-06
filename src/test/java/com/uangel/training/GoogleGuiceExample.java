package com.uangel.training;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import javax.inject.Singleton;

public class GoogleGuiceExample {
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new AppInjector());
        MyController app = injector.getInstance(MyController.class); // at this moment the data was injected
        System.out.println(app.getDatabaseData());
    }

    @Test
    public void testMySql() {
        Injector injector = Guice.createInjector(new AppInjector1());
        MyController app = injector.getInstance(MyController.class); // at this moment the data was injected
        System.out.println(app.getDatabaseData());
    }
}

class MyController {
    @Inject //inject directly the property
    private Database db;
    //  @Inject //injcting by param with setter methode. Or you can inject the data by contructor injection
//  public void readDatabaseData(Database db) {
//    this.db = db;
//  }
    public String getDatabaseData() {
        return db.getData();
    }
}

// configure which database should be used!
class AppInjector extends AbstractModule {
    @Override
    protected void configure() {
        bind(Database.class).to(InMemoryDatabase.class);
    }
}

class AppInjector1 extends AbstractModule {
    @Override
    protected void configure() {
        bind(Database.class).to(MySqlDatabase.class);
    }
}

interface Database {
    public String getData();
}

@Singleton
class InMemoryDatabase implements Database {
    @Override
    public String getData() {
        return "data from InMemoryDatabase";
    }
}

@Singleton
class MySqlDatabase implements Database {
    @Override
    public String getData() {
        return "data from MySqlDatabase";
    }
}
