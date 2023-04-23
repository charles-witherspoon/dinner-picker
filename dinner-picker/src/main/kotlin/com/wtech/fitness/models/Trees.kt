package com.wtech.fitness.models

import com.wtech.fitness.models.RedBlackNode.Direction.*


interface Node<T : Comparable<T>, R> {
    val key: T
    val value: R
    var parent: Node<T, R>
    var leftChild: Node<T, R>
    var rightChild: Node<T, R>
    fun nextNode(value: T): Node<T, R>
}


class NilNode<T: Comparable<T>, R>(parent: Node<T, R>? = null): Node<T, R> {
    override val key: T
        get() = error("NilNode has no key")
    override val value: R
        get() = error("NilNode has no value")
    override var parent: Node<T, R> = parent ?: this
    override var leftChild: Node<T, R> = this
    override var rightChild: Node<T, R> = this
    override fun nextNode(value: T): Node<T, R> = this
    override fun equals(other: Any?): Boolean {
        return other is NilNode<*, *>
    }
    override fun hashCode(): Int {
        return 0
    }

    override fun toString(): String {
        return "Nil"
    }
}

fun <T: Comparable<T>, R> Node<T, R>.pathTo(descendent: RedBlackNode<T, R>): List<Node<T, R>> {
    val path: MutableList<Node<T, R>> = mutableListOf(this)

    var current: Node<T, R> = this
    while (current != descendent && current !is NilNode) {
        current = current.nextNode(descendent.key)
        path.add(current)
    }

    return path
}

fun <T: Comparable<T>, R> Node<T, R>.swapChild(oldChild: Node<T, R>, newChild: Node<T, R>) {
    if (leftChild == oldChild) {
        leftChild = newChild
    } else if (rightChild == oldChild) {
        rightChild = newChild
    }
}

fun <T: Comparable<T>, R> Node<T, R>.rotate(
    direction: RedBlackNode.Direction
) {
    val x: Node<T, R> = this
    val y: Node<T, R> = when (direction) {
        LEFT -> x.rightChild
        RIGHT -> x.leftChild
    }
    val z: Node<T, R> = when (direction) {
        LEFT -> y.leftChild
        RIGHT -> y.rightChild
    }
    val p: Node<T, R> = x.parent

    p.swapChild(x, y)

    when (direction) {
        LEFT -> x.rightChild = z
        RIGHT -> x.leftChild = z
    }

    y.parent = p
    x.parent = y

    when (direction) {
        LEFT -> y.leftChild = x
        RIGHT -> y.rightChild = x
    }

    z.parent = x
}


open class RedBlackNode<T : Comparable<T>, R>(
    override val key: T,
    override val value: R,
    override var parent: Node<T, R> = NilNode(),
    override var leftChild: Node<T, R> = NilNode(),
    override var rightChild: Node<T, R> = NilNode()
) : Node<T, R> {

    enum class Direction { LEFT, RIGHT }

    private var _color: NodeColor = NodeColor.RED
    val color get() = _color

    val grandparent: Node<T, R> get() = parent.parent

    val uncle: Node<T, R>
        get() {
            return (grandparent as? RedBlackNode).run {
                if (this?.leftChild != parent) this?.leftChild
                else this.rightChild
            } ?: NilNode()
        }

    override fun nextNode(value: T): Node<T, R> = if (value < this.key) this.leftChild else this.rightChild

    override fun equals(other: Any?): Boolean {
        return (other !is NilNode<*, *>) && (other as? Node<*, *>)?.key == key
    }

    override fun hashCode(): Int {
        var hash = 5
        hash = 89 * hash + _color.hashCode()
        hash = 89 * hash + key.hashCode()
        return hash
    }

    override fun toString(): String {
        return key.toString()
    }
    fun recolor() {
        _color = if (_color == NodeColor.RED) NodeColor.BLACK else NodeColor.RED
    }
}

enum class NodeColor { RED, BLACK }

open class RedBlackTree<T : Comparable<T>, R> {

    var root: Node<T, R> = NilNode()

    private fun Node<T, R>.descendents(): List<Node<T, R>> {
        if (this is NilNode) return emptyList()

        val children = listOf(leftChild, rightChild)
        return children + children.flatMap { child -> child.descendents() }
    }

    fun getNodes(): List<Node<T, R>> {
        return listOf(root) + (root.descendents())
    }

    fun getLeaves(): List<Node<T, R>> = getNodes().filter { node ->
        listOf(node.leftChild, node.rightChild).all { it is NilNode }
    }

    fun insert(node: RedBlackNode<T, R>) {
        if (root is NilNode) {
            node.recolor()
            root = node
        } else {
            val leaf: Node<T, R> = getLeaf(node)
            check(leaf !is NilNode)
            node.parent = leaf

            if (node.key < leaf.key) {
                leaf.leftChild = node
            } else {
                leaf.rightChild = node
            }
        }

        val isViolation = node.color == NodeColor.RED &&
                (node == root || (node.parent as? RedBlackNode<T, R>)?.color == NodeColor.RED )
        if (isViolation) {
            var violatingNode: Node<T, R> = node
            while (violatingNode !is NilNode) {
                violatingNode = fixTree(violatingNode as RedBlackNode<T, R>)
            }
        }

    }

    fun find(value: T): Node<T, R> {
        if (this.root is NilNode) {
            return root
        }

        var current: Node<T, R> = this.root
        while (current !is NilNode && current.key != value) {
            current = current.nextNode(value)
        }

        return current
    }

    private fun getLeaf(node: RedBlackNode<T, R>): Node<T, R> {
        var currentNode: Node<T, R> = root
        var parent: Node<T, R> = currentNode.parent

        while (currentNode !is NilNode) {
            parent = currentNode
            currentNode = currentNode.nextNode(node.key)
        }
        return parent
    }

    private fun handleBlackUncle(z: RedBlackNode<T, R>): Node<T, R> {
        val isLeftChild = (z.parent.leftChild == z)

        val isTriangle =
            (isLeftChild && z.parent == z.grandparent.rightChild) || (!isLeftChild && z.parent == z.grandparent.leftChild)
        val isLine =
            (isLeftChild && z.parent == z.grandparent.leftChild) || (!isLeftChild && z.parent == z.grandparent.rightChild)

        when {
            isTriangle -> if (z.parent.leftChild == z) z.parent.rotate(RIGHT) else z.parent.rotate(LEFT)
            isLine -> if (z.parent.rightChild == z) z.grandparent.rotate(LEFT) else z.grandparent.rotate(RIGHT)
        }

        return when {
            z.color == NodeColor.RED && (z.parent as? RedBlackNode)?.color == NodeColor.RED -> z
            else -> NilNode()
        }
    }

    private fun handleRedUncle(node: RedBlackNode<T, R>): Node<T, R> {
        listOf(node.parent, node.grandparent, node.uncle)
            .forEach { (it as? RedBlackNode)?.recolor() }

        return if ((root as? RedBlackNode)?.color == NodeColor.RED) root else NilNode()
    }

    private fun fixTree(z: RedBlackNode<T, R>): Node<T, R> {
        if (z == root) {
            z.recolor()
            return NilNode()
        }

        return when ((z.uncle as? RedBlackNode)?.color) {
            NodeColor.RED -> handleRedUncle(z)
            else -> handleBlackUncle(z)
        }
    }

    fun delete(redBlackNode: RedBlackNode<T, R>) {
        TODO()
    }

    fun transplant(u: RedBlackNode<T, R>, v: RedBlackNode<T, R>) {
        when (u) {
            root -> root = v
            u.parent.leftChild -> u.parent.leftChild = v
            u.parent.rightChild -> u.parent.rightChild = v
        }
        v.parent = u.parent
    }
}

class IntervalTree<T : Comparable<T>, R> : RedBlackTree<T, R>() {

    open inner class ITNode<T : Comparable<T>, R>(open val lo: T, val hi: T, value: R, var maxEndpoint: T = hi) : RedBlackNode<T, R>(lo, value) {
        fun contains(value: T): Boolean = value >= lo && value < hi
    }


    fun insert(node: ITNode<T, R>) {
        super.insert(node)

        var current: ITNode<T, R> = node
        while (current != root) {
            val parent: ITNode<T, R> = current.parent as ITNode<T, R>

            if (current.maxEndpoint > parent.maxEndpoint) {
                parent.maxEndpoint = current.maxEndpoint
            }

            current = parent
        }
    }

    val Node<T, R>.lo: Comparable<*> get() {
        return (this as IntervalTree<*, *>.ITNode<*, *>).lo
    }

    private inline fun <reified IT: Node<*,*>> RedBlackNode<T, R>.asITNode(itNode: IT): IT? {
        if (this is IT) {
            return this
        }
        return null
    }

    fun getIntervalContaining(t: T): Node<out T, out R> {
        // if interval in node intersects query interval, then return it
        // else if left subtree is null, go right
        // else if max endpoint in left subtree is less than current node lo, go right
        // else go left
        var currentNode: Node<T, R> = root
        if (currentNode is NilNode) return currentNode

        while (currentNode !is NilNode) {
            currentNode = when {
                (currentNode as? IntervalTree<T, R>.ITNode<T, R>)?.contains(t) ?: false -> break
                currentNode.leftChild is NilNode -> currentNode.rightChild
                maxEndpointInLeftSubtreeIsLessThanCurrentNodeLo(currentNode, t)  -> currentNode.rightChild
                else -> currentNode.leftChild
            }
        }
        return currentNode
    }

    private fun maxEndpointInLeftSubtreeIsLessThanCurrentNodeLo(currentNode: Node<T, R>, value: T): Boolean {
        val maxEndpointInLeftSubtree: T? = (currentNode.leftChild as? ITNode<T, R>)?.maxEndpoint
        return maxEndpointInLeftSubtree != null  && maxEndpointInLeftSubtree < value
    }
}