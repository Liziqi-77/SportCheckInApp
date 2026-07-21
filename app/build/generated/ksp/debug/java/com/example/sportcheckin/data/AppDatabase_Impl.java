package com.example.sportcheckin.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile ExerciseProjectDao _exerciseProjectDao;

  private volatile CheckInRecordDao _checkInRecordDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `exercise_projects` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `targetMinutes` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `sortOrder` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `check_in_records` (`projectId` INTEGER NOT NULL, `date` TEXT NOT NULL, `completed` INTEGER NOT NULL, `minutes` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`projectId`, `date`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_check_in_records_date` ON `check_in_records` (`date`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_check_in_records_projectId` ON `check_in_records` (`projectId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '12629567636f2c1f3cf5851f92b0d944')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `exercise_projects`");
        db.execSQL("DROP TABLE IF EXISTS `check_in_records`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsExerciseProjects = new HashMap<String, TableInfo.Column>(5);
        _columnsExerciseProjects.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExerciseProjects.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExerciseProjects.put("targetMinutes", new TableInfo.Column("targetMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExerciseProjects.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExerciseProjects.put("sortOrder", new TableInfo.Column("sortOrder", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysExerciseProjects = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesExerciseProjects = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoExerciseProjects = new TableInfo("exercise_projects", _columnsExerciseProjects, _foreignKeysExerciseProjects, _indicesExerciseProjects);
        final TableInfo _existingExerciseProjects = TableInfo.read(db, "exercise_projects");
        if (!_infoExerciseProjects.equals(_existingExerciseProjects)) {
          return new RoomOpenHelper.ValidationResult(false, "exercise_projects(com.example.sportcheckin.data.ExerciseProjectEntity).\n"
                  + " Expected:\n" + _infoExerciseProjects + "\n"
                  + " Found:\n" + _existingExerciseProjects);
        }
        final HashMap<String, TableInfo.Column> _columnsCheckInRecords = new HashMap<String, TableInfo.Column>(5);
        _columnsCheckInRecords.put("projectId", new TableInfo.Column("projectId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCheckInRecords.put("date", new TableInfo.Column("date", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCheckInRecords.put("completed", new TableInfo.Column("completed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCheckInRecords.put("minutes", new TableInfo.Column("minutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCheckInRecords.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCheckInRecords = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCheckInRecords = new HashSet<TableInfo.Index>(2);
        _indicesCheckInRecords.add(new TableInfo.Index("index_check_in_records_date", false, Arrays.asList("date"), Arrays.asList("ASC")));
        _indicesCheckInRecords.add(new TableInfo.Index("index_check_in_records_projectId", false, Arrays.asList("projectId"), Arrays.asList("ASC")));
        final TableInfo _infoCheckInRecords = new TableInfo("check_in_records", _columnsCheckInRecords, _foreignKeysCheckInRecords, _indicesCheckInRecords);
        final TableInfo _existingCheckInRecords = TableInfo.read(db, "check_in_records");
        if (!_infoCheckInRecords.equals(_existingCheckInRecords)) {
          return new RoomOpenHelper.ValidationResult(false, "check_in_records(com.example.sportcheckin.data.CheckInRecordEntity).\n"
                  + " Expected:\n" + _infoCheckInRecords + "\n"
                  + " Found:\n" + _existingCheckInRecords);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "12629567636f2c1f3cf5851f92b0d944", "e7c6841765cfe5a7927721aba0047316");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "exercise_projects","check_in_records");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `exercise_projects`");
      _db.execSQL("DELETE FROM `check_in_records`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ExerciseProjectDao.class, ExerciseProjectDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CheckInRecordDao.class, CheckInRecordDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ExerciseProjectDao exerciseProjectDao() {
    if (_exerciseProjectDao != null) {
      return _exerciseProjectDao;
    } else {
      synchronized(this) {
        if(_exerciseProjectDao == null) {
          _exerciseProjectDao = new ExerciseProjectDao_Impl(this);
        }
        return _exerciseProjectDao;
      }
    }
  }

  @Override
  public CheckInRecordDao checkInRecordDao() {
    if (_checkInRecordDao != null) {
      return _checkInRecordDao;
    } else {
      synchronized(this) {
        if(_checkInRecordDao == null) {
          _checkInRecordDao = new CheckInRecordDao_Impl(this);
        }
        return _checkInRecordDao;
      }
    }
  }
}
