<?php


class OrderService
{
    public $onSave = array();

    public $onAdd = array();

    public $onStatusChanged = array();



    public function doSomeChange()
    {
        $order = 'suppose order here';
        $this->onSave($order); // event called
    }

}

$oser = new OrderService();
$oser->onSave();
