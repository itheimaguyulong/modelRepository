package com.winjoin;

import java.util.*;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class ObjectPool {
    private final int numObjects = 10;
    private final int maxObjects = 50;
    private Vector objects = null;

    //创建对象池
    public synchronized void createPool() {
        if (objects != null) {
            return;
        }
        //创建保存对象的向量
        objects = new Vector();
        for (int i = 0; i < numObjects; i++) {
            if (this.objects.size() < this.maxObjects) {
                PooledObject obj = new DefaultPooledObject(new GameObject());
                objects.addElement(obj);
            }
        }
    }

    //添加对象
    public synchronized void addObject(int num) throws Exception {
        for (int i = 0; i < num; i++) {
            if (this.objects.size() < this.maxObjects) {
                PooledObject obj = new DefaultPooledObject(new GameObject());
                objects.addElement(obj);
            } else {
                System.exit(0);
            }
        }
    }

    //获得对象
    public synchronized Object getObject() throws Exception {
        if (objects == null) {
            return null;
        }
        Object obj = getFreeObject();
        while (obj == null) {
            wait(250);
            obj = getFreeObject();
        }
        return obj;
    }

    //从对象池对象objects中返回一个可用的对象,如果没有对象就创建对象,并添加到对象池中
    private Object getFreeObject() throws Exception {
        //从线程池中获得可用的对象
        Object obj = this.findFreeObject();
        if (obj == null) {
            //如果获得的对象为空, 就添加对象
            addObject(numObjects);
            obj = this.findFreeObject();
            if (obj == null) {
                return null;
            }
        }
        return obj;
    }

    //从对象池中发现可用的对象
    private Object findFreeObject() {
        PooledObject pooledObject = null;
        Object obj = null;
        if (this.objects == null) {
            return null;
        }
        Enumeration elements = this.objects.elements();
        while (elements.hasMoreElements()) {
            pooledObject = (PooledObject) elements.nextElement();
            //allocate()如果在队列中,未使用,就返回这个对象
            if (pooledObject.allocate()) {
                obj = pooledObject.getObject();
                return obj;
            }
        }
        return null;
    }

    //返回一个对象到对象池中,并把该对象改为空闲,所有使用对象池获得的对象都不应该在使用的时候返回它  markReturning()
    public void returnObject(Object obj) {
        PooledObject pooledObject = null;
        if (objects == null) {
            return;
        }
        Enumeration elements = this.objects.elements();
        while (elements.hasMoreElements()) {
            pooledObject = (PooledObject) elements.nextElement();
            if (pooledObject.getObject() == obj) {
                //设为空闲对象
                pooledObject.deallocate();
            }
        }
    }

    //关闭对象池中所有的对象,并清空对象池
    public void closeObjects() {
        PooledObject pooledObject = null;
        if (objects == null) {
            return;
        }
        Enumeration enumerate = objects.elements();
        while (enumerate.hasMoreElements()) {
            pooledObject = (PooledObject) enumerate.nextElement();
            pooledObject.markAbandoned();
        }
    }

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        Class<?> poolClass = Class.forName("com.winjoin.ObjectPool");
        ObjectPool objectPool = (ObjectPool) poolClass.newInstance();
        objectPool.createPool();
        GameObject object1 = (GameObject) objectPool.getObject();
        object1.setGameName("英雄联盟");
        objectPool.returnObject(object1);
        GameObject object2 = (GameObject) objectPool.getObject();
        object2.setGameName("吃鸡");
        objectPool.returnObject(object2);
        GameObject object3 = (GameObject) objectPool.getObject();
        object3.setGameName("王者");
        objectPool.returnObject(object3);
        GameObject object4 = (GameObject) objectPool.getObject();
        object4.setGameName("穿越");
        objectPool.returnObject(object4);
        GameObject object5 = (GameObject) objectPool.getObject();
        object5.setGameName("和平");
        objectPool.returnObject(object5);
        long endTime = System.currentTimeMillis();
        System.out.println(endTime-startTime);
        GameObject object6 = null;
        try {
            object6 = (GameObject) objectPool.getObject();
            object6.setGameName("荒野");
            objectPool.closeObjects();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }finally {
            System.out.println(object1);
            System.out.println(object2);
            System.out.println(object3);
            System.out.println(object4);
            System.out.println(object5);
            System.out.println(object6);
        }
    }
}