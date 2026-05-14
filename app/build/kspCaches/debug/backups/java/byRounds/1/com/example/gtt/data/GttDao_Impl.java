package com.example.gtt.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class GttDao_Impl implements GttDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<LocationEntity> __insertionAdapterOfLocationEntity;

  private final EntityInsertionAdapter<VisitEntity> __insertionAdapterOfVisitEntity;

  private final EntityDeletionOrUpdateAdapter<VisitEntity> __updateAdapterOfVisitEntity;

  public GttDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfLocationEntity = new EntityInsertionAdapter<LocationEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `locations` (`id`,`name`,`latitude`,`longitude`,`radiusMeters`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LocationEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindDouble(3, entity.getLatitude());
        statement.bindDouble(4, entity.getLongitude());
        statement.bindDouble(5, entity.getRadiusMeters());
      }
    };
    this.__insertionAdapterOfVisitEntity = new EntityInsertionAdapter<VisitEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `visits` (`id`,`locationId`,`entryTime`,`exitTime`,`isManualPunchOut`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final VisitEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getLocationId());
        statement.bindLong(3, entity.getEntryTime());
        if (entity.getExitTime() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getExitTime());
        }
        final int _tmp = entity.isManualPunchOut() ? 1 : 0;
        statement.bindLong(5, _tmp);
      }
    };
    this.__updateAdapterOfVisitEntity = new EntityDeletionOrUpdateAdapter<VisitEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `visits` SET `id` = ?,`locationId` = ?,`entryTime` = ?,`exitTime` = ?,`isManualPunchOut` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final VisitEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getLocationId());
        statement.bindLong(3, entity.getEntryTime());
        if (entity.getExitTime() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getExitTime());
        }
        final int _tmp = entity.isManualPunchOut() ? 1 : 0;
        statement.bindLong(5, _tmp);
        statement.bindLong(6, entity.getId());
      }
    };
  }

  @Override
  public Object insertLocation(final LocationEntity location,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfLocationEntity.insertAndReturnId(location);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertVisit(final VisitEntity visit, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfVisitEntity.insertAndReturnId(visit);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateVisit(final VisitEntity visit, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfVisitEntity.handle(visit);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<LocationEntity>> getAllLocations() {
    final String _sql = "SELECT * FROM locations";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"locations"}, new Callable<List<LocationEntity>>() {
      @Override
      @NonNull
      public List<LocationEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfRadiusMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "radiusMeters");
          final List<LocationEntity> _result = new ArrayList<LocationEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LocationEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final float _tmpRadiusMeters;
            _tmpRadiusMeters = _cursor.getFloat(_cursorIndexOfRadiusMeters);
            _item = new LocationEntity(_tmpId,_tmpName,_tmpLatitude,_tmpLongitude,_tmpRadiusMeters);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getLocationById(final int id,
      final Continuation<? super LocationEntity> $completion) {
    final String _sql = "SELECT * FROM locations WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<LocationEntity>() {
      @Override
      @Nullable
      public LocationEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfRadiusMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "radiusMeters");
          final LocationEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final float _tmpRadiusMeters;
            _tmpRadiusMeters = _cursor.getFloat(_cursorIndexOfRadiusMeters);
            _result = new LocationEntity(_tmpId,_tmpName,_tmpLatitude,_tmpLongitude,_tmpRadiusMeters);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<VisitEntity>> getVisitsForLocation(final int locationId) {
    final String _sql = "SELECT * FROM visits WHERE locationId = ? ORDER BY entryTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, locationId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"visits"}, new Callable<List<VisitEntity>>() {
      @Override
      @NonNull
      public List<VisitEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfLocationId = CursorUtil.getColumnIndexOrThrow(_cursor, "locationId");
          final int _cursorIndexOfEntryTime = CursorUtil.getColumnIndexOrThrow(_cursor, "entryTime");
          final int _cursorIndexOfExitTime = CursorUtil.getColumnIndexOrThrow(_cursor, "exitTime");
          final int _cursorIndexOfIsManualPunchOut = CursorUtil.getColumnIndexOrThrow(_cursor, "isManualPunchOut");
          final List<VisitEntity> _result = new ArrayList<VisitEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final VisitEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpLocationId;
            _tmpLocationId = _cursor.getInt(_cursorIndexOfLocationId);
            final long _tmpEntryTime;
            _tmpEntryTime = _cursor.getLong(_cursorIndexOfEntryTime);
            final Long _tmpExitTime;
            if (_cursor.isNull(_cursorIndexOfExitTime)) {
              _tmpExitTime = null;
            } else {
              _tmpExitTime = _cursor.getLong(_cursorIndexOfExitTime);
            }
            final boolean _tmpIsManualPunchOut;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsManualPunchOut);
            _tmpIsManualPunchOut = _tmp != 0;
            _item = new VisitEntity(_tmpId,_tmpLocationId,_tmpEntryTime,_tmpExitTime,_tmpIsManualPunchOut);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<VisitEntity>> getAllVisits() {
    final String _sql = "SELECT * FROM visits ORDER BY entryTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"visits"}, new Callable<List<VisitEntity>>() {
      @Override
      @NonNull
      public List<VisitEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfLocationId = CursorUtil.getColumnIndexOrThrow(_cursor, "locationId");
          final int _cursorIndexOfEntryTime = CursorUtil.getColumnIndexOrThrow(_cursor, "entryTime");
          final int _cursorIndexOfExitTime = CursorUtil.getColumnIndexOrThrow(_cursor, "exitTime");
          final int _cursorIndexOfIsManualPunchOut = CursorUtil.getColumnIndexOrThrow(_cursor, "isManualPunchOut");
          final List<VisitEntity> _result = new ArrayList<VisitEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final VisitEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpLocationId;
            _tmpLocationId = _cursor.getInt(_cursorIndexOfLocationId);
            final long _tmpEntryTime;
            _tmpEntryTime = _cursor.getLong(_cursorIndexOfEntryTime);
            final Long _tmpExitTime;
            if (_cursor.isNull(_cursorIndexOfExitTime)) {
              _tmpExitTime = null;
            } else {
              _tmpExitTime = _cursor.getLong(_cursorIndexOfExitTime);
            }
            final boolean _tmpIsManualPunchOut;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsManualPunchOut);
            _tmpIsManualPunchOut = _tmp != 0;
            _item = new VisitEntity(_tmpId,_tmpLocationId,_tmpEntryTime,_tmpExitTime,_tmpIsManualPunchOut);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getActiveVisit(final Continuation<? super VisitEntity> $completion) {
    final String _sql = "SELECT * FROM visits WHERE exitTime IS NULL LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<VisitEntity>() {
      @Override
      @Nullable
      public VisitEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfLocationId = CursorUtil.getColumnIndexOrThrow(_cursor, "locationId");
          final int _cursorIndexOfEntryTime = CursorUtil.getColumnIndexOrThrow(_cursor, "entryTime");
          final int _cursorIndexOfExitTime = CursorUtil.getColumnIndexOrThrow(_cursor, "exitTime");
          final int _cursorIndexOfIsManualPunchOut = CursorUtil.getColumnIndexOrThrow(_cursor, "isManualPunchOut");
          final VisitEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpLocationId;
            _tmpLocationId = _cursor.getInt(_cursorIndexOfLocationId);
            final long _tmpEntryTime;
            _tmpEntryTime = _cursor.getLong(_cursorIndexOfEntryTime);
            final Long _tmpExitTime;
            if (_cursor.isNull(_cursorIndexOfExitTime)) {
              _tmpExitTime = null;
            } else {
              _tmpExitTime = _cursor.getLong(_cursorIndexOfExitTime);
            }
            final boolean _tmpIsManualPunchOut;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsManualPunchOut);
            _tmpIsManualPunchOut = _tmp != 0;
            _result = new VisitEntity(_tmpId,_tmpLocationId,_tmpEntryTime,_tmpExitTime,_tmpIsManualPunchOut);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<VisitEntity> getActiveVisitFlow() {
    final String _sql = "SELECT * FROM visits WHERE exitTime IS NULL";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"visits"}, new Callable<VisitEntity>() {
      @Override
      @Nullable
      public VisitEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfLocationId = CursorUtil.getColumnIndexOrThrow(_cursor, "locationId");
          final int _cursorIndexOfEntryTime = CursorUtil.getColumnIndexOrThrow(_cursor, "entryTime");
          final int _cursorIndexOfExitTime = CursorUtil.getColumnIndexOrThrow(_cursor, "exitTime");
          final int _cursorIndexOfIsManualPunchOut = CursorUtil.getColumnIndexOrThrow(_cursor, "isManualPunchOut");
          final VisitEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpLocationId;
            _tmpLocationId = _cursor.getInt(_cursorIndexOfLocationId);
            final long _tmpEntryTime;
            _tmpEntryTime = _cursor.getLong(_cursorIndexOfEntryTime);
            final Long _tmpExitTime;
            if (_cursor.isNull(_cursorIndexOfExitTime)) {
              _tmpExitTime = null;
            } else {
              _tmpExitTime = _cursor.getLong(_cursorIndexOfExitTime);
            }
            final boolean _tmpIsManualPunchOut;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsManualPunchOut);
            _tmpIsManualPunchOut = _tmp != 0;
            _result = new VisitEntity(_tmpId,_tmpLocationId,_tmpEntryTime,_tmpExitTime,_tmpIsManualPunchOut);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
