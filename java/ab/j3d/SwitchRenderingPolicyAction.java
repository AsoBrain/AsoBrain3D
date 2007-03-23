/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2007-2007 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d;

import java.util.Locale;

import ab.j3d.view.ViewModelView;

import com.numdata.oss.ResourceBundleTools;
import com.numdata.oss.ArrayTools;
import com.numdata.oss.ui.ChoiceAction;

/**
 * This action switches the rendering policy of a {@link ViewModelView}.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class SwitchRenderingPolicyAction
	extends ChoiceAction
{
	/**
	 * The {@link ViewModelView} this action belongs to.
	 */
	final ViewModelView _view;

	/**
	 * Rendering policies that are supported by the view.
	 */
	final int[] _supportedPolicies;

	/**
	 * Construct a new action to switch the rendering policy of a view.
	 *
	 * @param   locale              Preferred locale for internationalization.
	 * @param   view                The view this action belongs to.
	 * @param   currentPolicy       Current rendering policy of the view.
	 * @param   supportedPolicies   Rendering policies that are supported by the view.
	 */
	public SwitchRenderingPolicyAction( final Locale locale , final ViewModelView view , final int currentPolicy , final int[] supportedPolicies )
	{
		super( ResourceBundleTools.getBundle( SwitchRenderingPolicyAction.class , locale ) , getPolicyNames( supportedPolicies ) , ArrayTools.indexOf( new Integer( currentPolicy ) , supportedPolicies ) );

		_view              = view;
		_supportedPolicies = supportedPolicies;
	}

	public void run()
	{
		final String selected       = getSelectedPolicyName();
		final int    selectedPolicy = "schematic".equals( selected ) ? ViewModelView.SCHEMATIC :
		                              "wireFrame".equals( selected ) ? ViewModelView.WIREFRAME :
		                              "sketch"   .equals( selected ) ? ViewModelView.SKETCH    : ViewModelView.SOLID;

		_view.setRenderingPolicy( selectedPolicy );
		_view.update();
	}

	/**
	 * Get {@link String} representation of the selected rendering policy.
	 *
	 * @return  {@link String} representation of the selected rendering policy.
	 */
	private String getSelectedPolicyName()
	{
		return getPolicyName( _supportedPolicies[ getSelectedIndex() ] );
	}

	/**
	 * Get {@link String} representations of the specified rendering policies.
	 *
	 * @param   policies    Policies to get {@link String} representations for.
	 *
	 * @return  {@link String} representations of the specified rendering policies.
	 */
	private static String[] getPolicyNames( final int[] policies )
	{
		final String[] result = new String[ policies.length ];

		for ( int i = 0 ; i < policies.length ; i++ )
			result[ i ] = getPolicyName( policies[ i ] );

		return result;
	}

	/**
	 * Get {@link String} representation of a rendering policy.
	 *
	 * @param   policy  Policy to get {@link String} representation for.
	 *
	 * @return  {@link String} representation of the specified rendering policy;
	 *          <code>null</code> if non-existing policy is specified.
	 */
	private static String getPolicyName( final int policy )
	{
		return ( policy == ViewModelView.SOLID     ) ? "solid"     :
	           ( policy == ViewModelView.SCHEMATIC ) ? "schematic" :
	           ( policy == ViewModelView.WIREFRAME ) ? "wireFrame" :
	           ( policy == ViewModelView.SKETCH    ) ? "sketch"    : null;
	}
}