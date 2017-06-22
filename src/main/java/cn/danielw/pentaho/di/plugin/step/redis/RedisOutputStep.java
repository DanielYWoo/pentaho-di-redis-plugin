package cn.danielw.pentaho.di.plugin.step.redis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RedisOutputStep extends BaseStep implements StepInterface {

    private static final int REDIS_TIMEOUT = 1000;
    private JedisCluster client;

    /**
	 * The constructor should simply pass on its arguments to the parent class.
	 * 
	 * @param s 				step description
	 * @param stepDataInterface	step data class
	 * @param c					step copy
	 * @param t					transformation description
	 * @param dis				transformation executing
	 */
	public RedisOutputStep(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
		super(s, stepDataInterface, c, t, dis);
	}
	
	/**
	 * This method is called by PDI during transformation startup. 
	 *
	 * @param smi 	step meta interface implementation, containing the step settings
	 * @param sdi	step data interface implementation, used to store runtime information
	 * 
	 * @return true if initialization completed successfully, false if there was an error preventing the step from working. 
	 *  
	 */
	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		// Casting to step-specific implementation classes is safe
		RedisOutputStepMeta meta = (RedisOutputStepMeta) smi;
		RedisOutputStepData data = (RedisOutputStepData) sdi;
        logBasic("creating redis session factory, addresses=" + meta.getUrl());
        String[] redisNodes = meta.getUrl().split(",");

        Set<HostAndPort> jedisClusterNodes = new HashSet<>();
        for (String redisNode: redisNodes) {
            String[] config = redisNode.split(":");
            jedisClusterNodes.add(new HostAndPort(config[0], Integer.parseInt(config[1])));
        }

        client = new JedisCluster(jedisClusterNodes, REDIS_TIMEOUT, new GenericObjectPoolConfig());
		return super.init(meta, data);
	}	

	/**
	 * Once the transformation starts executing, the processRow() method is called repeatedly
	 * by PDI for as long as it returns true. To indicate that a step has finished processing rows
	 * this method must call setOutputDone() and return false;
	 * 
	 * Steps which process incoming rows typically call getRow() to read a single row from the
	 * input stream, change or add row content, call putRow() to pass the changed row on
	 * and return true. If getRow() returns null, no more rows are expected to come in, 
	 * and the processRow() implementation calls setOutputDone() and returns false to
	 * indicate that it is done too.
	 * 
	 * Steps which generate rows typically construct a new row Object[] using a call to
	 * RowDataUtil.allocateRowData(numberOfFields), add row content, and call putRow() to
	 * pass the new row on. Above process may happen in a loop to generate multiple rows,
	 * at the end of which processRow() would call setOutputDone() and return false;
	 * 
	 * @param smi the step meta interface containing the step settings
	 * @param sdi the step data interface that should be used to store
	 * 
	 * @return true to indicate that the function should be called again, false if the step is done
	 */
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {

		// safely cast the step settings (meta) and runtime info (data) to specific implementations 
		RedisOutputStepMeta meta = (RedisOutputStepMeta) smi;
		RedisOutputStepData data = (RedisOutputStepData) sdi;

		// get incoming row, getRow() potentially blocks waiting for more rows, returns null if no more rows expected
		Object[] r = getRow(); 
		
		// if no more rows are expected, indicate step is finished and processRow() should not be called again
		if (r == null){
			setOutputDone();
			return false;
		}

        if (first) {
            first = false;
            data.setOutputRowMeta(getInputRowMeta().clone());
            meta.getFields(data.getOutputRowMeta(), getStepname(), null, null, this, null, null);
        }

		sendCommand(meta.getCommand(), r);

		// put the row to the output row stream
		putRow(data.getOutputRowMeta(), r);

		// log progress if it is time to to so
		if (checkFeedback(getLinesRead())) {
			logBasic("Rows read: " + getLinesRead()); // Some basic logging
		}
		return true;
	}

    private void sendCommand(String command, Object[] row) {
	    switch (command) {
            case "set":
                client.set(row[0].toString(), row[1].toString());
                break;
            case "del":
                client.del(row[0].toString());
                break;
            case "sadd":
                client.sadd(row[0].toString(), row[1].toString());
                break;
            case "srem":
                client.srem(row[0].toString(), row[1].toString());
                break;
            case "zadd":
                client.zadd(row[0].toString(), Double.parseDouble(row[1].toString()), row[2].toString());
                break;
            case "zrem":
                client.zrem(row[0].toString(), row[1].toString());
                break;
            default:
                throw new IllegalArgumentException("Not supported command " + command);
        }
    }

    /**
	 * This method is called by PDI once the step is done processing. 
	 * 
	 * The dispose() method is the counterpart to init() and should release any resources
	 * acquired for step execution like file handles or database connections.
	 * 
	 * The meta and data implementations passed in can safely be cast
	 * to the step's respective implementations. 
	 * 
	 * It is mandatory that super.dispose() is called to ensure correct behavior.
	 * 
	 * @param smi 	step meta interface implementation, containing the step settings
	 * @param sdi	step data interface implementation, used to store runtime information
	 */
	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {

		// Casting to step-specific implementation classes is safe
		RedisOutputStepMeta meta = (RedisOutputStepMeta) smi;
		RedisOutputStepData data = (RedisOutputStepData) sdi;
		
		super.dispose(meta, data);
	}

}
