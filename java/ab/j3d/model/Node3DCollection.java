package common.renderer;

import common.model.Matrix3D;

/**
 * This collection class is used to store combinations of Matrix3D
 * and TreeNode objects. 
 *
 * @version 1.0 (20011128, PSH) 
 * @author	Peter S. Heijnen
 */
public class LeafCollection
{
	/**
	 * Number of stored elements.
	 */
	int _elementCount = 0;

	/**
	 * Array with Matrix3D objects in collection. This size of this
	 * array may exceed the element count, but is never smaller.
	 */
	Matrix3D[] _matrixData = new Matrix3D[ 10 ];

	/**
	 * Array with TreeNode objects in collection. This size of this
	 * array may exceed the element count, but is never smaller.
	 */
	TreeNode[] _nodeData = new TreeNode[ 10 ];
	
	/**
	 * Appends the specified node to the end of this collection.
	 *
	 * @param	matrix	Matrix3D associated with node.
	 * @param	node	Node to add.
	 *
	 * @since JDK1.2
	 */
	public synchronized void add( Matrix3D matrix , TreeNode node )
	{
		ensureCapacity( _elementCount + 1 );

		_matrixData[ _elementCount   ] = matrix;
		_nodeData  [ _elementCount++ ] = node;
	}

	/**
	 * Removes all elements and sets the size to zero.
	 */
	public void clear()
	{
		removeAllElements();
	}

	/**
	 * Increases the capacity of this collection, if necessary, to ensure 
	 * that it can hold at least the number of components specified by 
	 * the minimum capacity argument.
	 *
	 * @param   minCapacity   The desired minimum capacity.
	 */
	public synchronized void ensureCapacity( int minCapacity )
	{
		int oldCapacity = _nodeData.length;
		
		if ( minCapacity > oldCapacity )
		{
			TreeNode	oldNodes[]		= _nodeData;
			Matrix3D		oldMatrices[]	= _matrixData;
			
			int newCapacity = oldCapacity * 2;

		    _nodeData   = new TreeNode[ newCapacity ];
		    _matrixData = new Matrix3D    [ newCapacity ];
		    
			System.arraycopy( oldNodes    , 0 , _nodeData   , 0 , _elementCount );
			System.arraycopy( oldMatrices , 0 , _matrixData , 0 , _elementCount );
		}
	}

	/**
	 * Get matrix at specified index.
	 *
	 * @param	index	Index of element.
	 *
	 * @return	Matrix3D object at specified index.
	 */
	public Matrix3D getMatrix( int index )
	{
		if ( index >= _elementCount )
		    throw new ArrayIndexOutOfBoundsException( index );

		return( _matrixData[ index ] );
	}

	/**
	 * Get node at specified index.
	 *
	 * @param	index	Index of element.
	 *
	 * @return	TreeNode object at specified index.
	 */
	public TreeNode getNode( int index )
	{
		if ( index >= _elementCount )
		    throw new ArrayIndexOutOfBoundsException( index );

		return( _nodeData[ index ] );
	}

	/**
	 * Removes all elements and sets the size to zero.
	 */
	public void removeAllElements()
	{
		for ( int i = 0 ; i < _elementCount ; i++ )
		{
			_nodeData  [ i ] = null;
			_matrixData[ i ] = null;
		}
		_elementCount = 0;
	}

	/**
	 * Returns the number of elements.
	 *
	 * @return  The number of elements.
	 */
	public int size()
	{
		return( _elementCount );
	}

}
