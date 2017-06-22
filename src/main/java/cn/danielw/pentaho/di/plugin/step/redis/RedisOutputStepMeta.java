package cn.danielw.pentaho.di.plugin.step.redis;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.util.List;

/**
 * This class is part of the demo step plug-in implementation.
 * It demonstrates the basics of developing a plug-in step for PDI. 
 * 
 * The demo step adds a new string field to the row stream and sets its
 * value to "Hello World!". The user may select the name of the new field.
 *   
 * This class is the implementation of StepMetaInterface.
 * Classes implementing this interface need to:
 * 
 * - keep track of the step settings
 * - serialize step settings both to xml and a repository
 * - provide new instances of objects implementing StepDialogInterface, StepInterface and StepDataInterface
 * - report on how the step modifies the meta-data of the row-stream (row structure and field types)
 * - perform a sanity-check on the settings provided by the user 
 * 
 */

@Step(	
		id = "RedisOutputStep",
		image = "cn/danielw/pentaho/di/plugin/step/redis/icon/redis.svg",
		i18nPackageName="cn.danielw.pentaho.di.plugin.step.redis",
		name="RedisOutputStep.Name",
		description="RedisOutputStep.Description",
		categoryDescription="i18n:org.pentaho.di.trans.step:BaseStep.Category.Output"
)
public class RedisOutputStepMeta extends BaseStepMeta implements StepMetaInterface, Cloneable {

    private static final String XML_TAG = "arguments";
	private static final String XML_KEY_COMMAND = "command";
    private static final String XML_KEY_URL = "url";
	private static Class<?> PKG = RedisOutputStepMeta.class; // for i18n purposes
	
	private String command;
    private String url;

    /**
	 * Constructor should call super() to make sure the base class has a chance to initialize properly.
	 */
	public RedisOutputStepMeta() {
		super(); 
	}
	
	/**
	 * Called by Spoon to get a new instance of the SWT dialog for the step.
	 */
	@SuppressWarnings("unused")
    public StepDialogInterface getDialog(Shell shell, StepMetaInterface meta, TransMeta transMeta, String name) {
        return new RedisOutputStepDialog(shell, meta, transMeta, name);
	}

	/**
	 * Called by PDI to get a new instance of the step implementation. 
	 * A standard implementation passing the arguments to the constructor of the step class is recommended.
	 */
    @Override
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp) {
		return new RedisOutputStep(stepMeta, stepDataInterface, cnr, transMeta, disp);
	}

	/**
	 * Called by PDI to get a new instance of the step data class.
	 */
    @Override
	public StepDataInterface getStepData() {
		return new RedisOutputStepData();
	}	

	/**
	 * This method is called every time a new step is created and should allocate/set the step configuration
	 * to sensible defaults. The values set here will be used by Spoon when a new step is created.    
	 */
    @Override
	public void setDefault() {
        command = "set";
		url = "10.0.0.1:6381,10.0.0.2:6381,10.0.0.3:6381";
	}
	
	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

	@Override
	public String getXML() throws KettleException {
        StringBuffer retval = new StringBuffer( 100 );
        retval.append( "  " ).append( XMLHandler.openTag( XML_TAG ) );
        retval.append( super.getXML() );
        retval.append( "    " ).append( XMLHandler.addTagValue(XML_KEY_COMMAND, getCommand()) );
        retval.append( "    " ).append( XMLHandler.addTagValue(XML_KEY_URL, getUrl() ) );
        retval.append( "  " ).append( XMLHandler.closeTag( XML_TAG ) );
        return retval.toString();
	}

    @Override
	public void loadXML(Node node, List<DatabaseMeta> databases, IMetaStore metaStore) throws KettleXMLException {
        super.loadXML( node, databases, metaStore );
        try {
            Node arguments = XMLHandler.getSubNode(node, XML_TAG);
            command = XMLHandler.getTagValue(arguments, XML_KEY_COMMAND);
            logBasic("load config: command=" + command);
            url = XMLHandler.getTagValue(arguments, XML_KEY_URL);
            logBasic("load config: url=" + url);

        } catch ( Exception e ) {
            throw new KettleXMLException("Redis plugin unable to read step info from XML node", e);
        }
	}

	@Override
    public Object clone() {
        return super.clone();
    }
	/*
//	 * This method is called by Spoon when a step needs to serialize its configuration to a repository.
//	 * The repository implementation provides the necessary methods to save the step attributes.
	public void saveRep(Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step) throws KettleException
	{
		try{
			rep.saveStepAttribute(id_transformation, id_step, XML_KEY_COMMAND, command); //$NON-NLS-1$
		}
		catch(Exception e){
			throw new KettleException("Unable to save step into repository: "+id_step, e); 
		}
	}		
	
//	 * This method is called by PDI when a step needs to read its configuration from a repository.
//	 * The repository implementation provides the necessary methods to read the step attributes.
	public void readRep(Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases) throws KettleException  {
		try{
			command = rep.getStepAttributeString(id_step, XML_KEY_COMMAND); //$NON-NLS-1$
		}
		catch(Exception e){
			throw new KettleException("Unable to load step from repository", e);
		}
	}
*/
	/**
	 * This method is called when the user selects the "Verify Transformation" option in Spoon. 
	 * A list of remarks is passed in that this method should add to. Each remark is a comment, warning, error, or ok.
	 * The method should perform as many checks as necessary to catch design-time errors.
	 * 
	 * Typical checks include:
	 * - verify that all mandatory configuration is given
	 * - verify that the step receives any input, unless it's a row generating step
	 * - verify that the step does not receive any input if it does not take them into account
	 * - verify that the step finds fields it relies on in the row-stream
	 */
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info, VariableSpace space, Repository repository, IMetaStore metaStore)  {
		
		CheckResult cr;

		// See if there are input streams leading to this step!
		if (input.length > 0) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK,
                    BaseMessages.getString(PKG, "RedisOutputStepMeta.CheckResult.ReceivingRows.OK"), stepMeta);
			remarks.add(cr);
		} else {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
                    BaseMessages.getString(PKG, "RedisOutputStepMeta.CheckResult.ReceivingRows.ERROR"), stepMeta);
			remarks.add(cr);
		}	
    	
	}


}
