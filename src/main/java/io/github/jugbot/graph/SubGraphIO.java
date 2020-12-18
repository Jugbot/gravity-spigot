package io.github.jugbot.graph;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Chunk;

import io.github.jugbot.App;
import io.github.jugbot.util.CacheIO;

public class SubGraphIO extends CacheIO<Chunk, SubGraph> {
  private static final boolean WIPE_DB = true;
  private static final String INIT_SQL =
      String.join(
          " ",
          "CREATE TABLE IF NOT EXISTS chunkmap(",
          "world VARCHAR(32) NOT NULL,",
          "x INTEGER NOT NULL,",
          "z INTEGER NOT NULL,",
          "object BLOB NOT NULL,",
          "PRIMARY KEY(x, z, world)",
          ");");
  private static final String READ_SQL = "SELECT object FROM chunkmap WHERE world=? AND x=? AND z=?;";
  private static final String WRITE_SQL = "INSERT OR REPLACE INTO chunkmap VALUES(?, ?, ?, ?);";
  private static SubGraphIO instance;
  private Connection connection;

  public static SubGraphIO Instance() {
    if (instance == null) instance = new SubGraphIO();
    return instance;
  }

  private SubGraphIO() {
    String url = "jdbc:sqlite:" + App.Instance().getDataFolder() + "/chunkdata.db";
    App.Instance().getLogger().fine("Connecting to database at " + url);
    try {
      connection = DriverManager.getConnection(url);
      if (connection != null) {
        DatabaseMetaData meta = connection.getMetaData();
        App.Instance().getLogger().fine("The driver name is " + meta.getDriverName());
        if (WIPE_DB) {
          connection.createStatement().execute("DROP TABLE IF EXISTS chunkmap;");
        }
        connection.createStatement().execute(INIT_SQL);
      } else {
        throw new SQLException();
      }
    } catch (SQLException e) {
      App.Instance().getLogger().fine(INIT_SQL);
      App.Instance().getLogger().fine(e.getMessage());
    }
  }

  public void saveChunk(SubGraph chunk) {
    App.Instance().getLogger().fine("Save Chunk");
    try {
      PreparedStatement statement = connection.prepareStatement(WRITE_SQL);
      statement.setString(1, chunk.getWorldName());
      statement.setInt(2, chunk.getX());
      statement.setInt(3, chunk.getZ());
      statement.setBytes(4, serialize(chunk));
      statement.execute();
    } catch (SQLException e) {
      App.Instance().getLogger().fine(WRITE_SQL);
      App.Instance().getLogger().fine(e.getMessage());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public SubGraph loadChunk(String worldName, int x, int z) {
    App.Instance().getLogger().fine("Load Chunk");
    try {
      PreparedStatement statement = connection.prepareStatement(READ_SQL);
      statement.setString(1, worldName);
      statement.setInt(2, x);
      statement.setInt(3, z);
      ResultSet set = statement.executeQuery();
      if (set.isClosed()) return null;
      return (SubGraph) deserialize(set.getBinaryStream("object"));
    } catch (SQLException e) {
      App.Instance().getLogger().fine(READ_SQL);
      App.Instance().getLogger().fine(e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private static Object deserialize(InputStream stream) throws Exception {
    ObjectInputStream ois = new ObjectInputStream(stream);
    try {
      return ois.readObject();
    } finally {
      ois.close();
    }
  }

  private static byte[] serialize(Object object) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(object);
    oos.close();
    return baos.toByteArray();
  }

  @Override
  public void write(Chunk key, SubGraph subGraph) {
    // TODO Auto-generated method stub

  }

  @Override
  public SubGraph read(Chunk key) {
    // TODO Auto-generated method stub
    return null;
  }
}
