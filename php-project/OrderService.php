<?php

class OrderService
{
    public $onSave = array();

    public $onAdd = array();

    public $onStatusChanged = array();

    /** @var EventManager */
    public $eventManager;

    public function doSomeChange()
    {
        $order = 'suppose order here';
        $this->onSave($order);
        $this->eventManager->dispatchEvent(Events::preFlush); //doctrine
        $this->eventManager->dispatch(Events::MY_FOO_EVENT); //symfony
    }

    

}

class Events {

    const MY_FOO_EVENT = 'events.foo';

    const preFlush = "preFlush";
}

$oser = new OrderService();
$oser->onSave();

