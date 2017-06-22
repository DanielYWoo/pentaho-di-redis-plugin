package cn.danielw.pentaho.di.plugin.step.redis;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * In addition classes implementing this interface usually keep track of
 * per-thread resources during step execution. Typical examples are:
 * result sets, temporary data, caching indexes, etc.
 */
public class RedisOutputStepData extends BaseStepData implements StepDataInterface {

	private RowMetaInterface outputRowMeta;
	
    public RedisOutputStepData()
	{
		super();
	}

	public RowMetaInterface getOutputRowMeta() {
		return outputRowMeta;
	}

	public void setOutputRowMeta(RowMetaInterface outputRowMeta) {
		this.outputRowMeta = outputRowMeta;
	}
}
	
